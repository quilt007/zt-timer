package world.ztomorrow.zttimer.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import world.ztomorrow.zttimer.common.conf.SchedulerAppConf;
import world.ztomorrow.zttimer.manager.RedisManager;
import world.ztomorrow.zttimer.service.trigger.TriggerWorker;
import world.ztomorrow.zttimer.utils.TimeUtils;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerTask {

    private final SchedulerAppConf schedulerAppConf;
    private final RedisManager redisManager;
    private final TriggerWorker triggerWorker;

    /**
     *  尝试获取分布式锁
     */
    @Async("schedulerPool")
    public void asyncHandleSlice(LocalDateTime time, int bucketId) {
        log.info("start executeAsync");
        String key = TimeUtils.GetTimeBucketLockKey(time, bucketId);
        String token = TimeUtils.GetTokenStr();
        boolean ok = redisManager.lock(key, token, schedulerAppConf.getTryLockSeconds());
        if(!ok){
            log.info("asyncHandleSlice 获取分布式锁失败");
            return;
        }

        // 调用trigger进行处理
        triggerWorker.work(TimeUtils.GetSliceMsgKey(time, bucketId));

        // 延长分布式锁的时间,避免重复执行分片
        redisManager.expireLock(key, token, schedulerAppConf.getSuccessExpireSeconds());

        log.info("end executeAsync");
    }
}
