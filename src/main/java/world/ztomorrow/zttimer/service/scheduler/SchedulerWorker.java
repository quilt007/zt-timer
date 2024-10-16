package world.ztomorrow.zttimer.service.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import world.ztomorrow.zttimer.common.conf.SchedulerAppConf;
import world.ztomorrow.zttimer.manager.RedisManager;
import world.ztomorrow.zttimer.service.trigger.TriggerWorker;
import world.ztomorrow.zttimer.utils.TimeUtils;

import java.time.LocalDateTime;

/**
 * 调度器
 */
@Component
@Slf4j
@AllArgsConstructor
public class SchedulerWorker {

    private final SchedulerAppConf schedulerAppConf;
    private final RedisManager redisManager;
    private final TriggerWorker triggerWorker;

    @Scheduled(fixedRate = 1000L)
    public void scheduledTask() {
        log.info("任务执行时间：{}", LocalDateTime.now());
        handleSlices();
    }

    /**
     * 每执行一次的时间分区
     */
    private void handleSlices() {
        for (int i = 0; i < schedulerAppConf.getBucketsNum(); i++) {
            handleSlice(i);
        }
    }

    /**
     *  尝试调度上时间分段，和本分段
     */
    private void handleSlice(int bucketId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime preTime = now.minusMinutes(1L);

        // 重试机制
        // 上一分钟失败会重试，成功则会因为锁过期时间延长而避免重试
        try {
            asyncHandleSlice(preTime, bucketId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            asyncHandleSlice(now, bucketId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  尝试获取分布式锁
     */
    private void asyncHandleSlice(LocalDateTime time, int bucketId) {
        log.info("start executeAsync");
        String key = TimeUtils.GetTimeBucketLockKey(time, bucketId);
        String token = TimeUtils.GetTokenStr();
        int expireLock = schedulerAppConf.getTryLockSeconds();
        boolean ok = redisManager.lock(key, token, expireLock);
        if(!ok){
            log.info("asyncHandleSlice 获取分布式锁失败");
            return;
        }

        // 调用trigger进行处理
        triggerWorker.work(TimeUtils.GetSliceMsgKey(time, bucketId));

        // 延长分布式锁的时间,避免重复执行分片
        redisManager.expireLock(key, token, expireLock);

        log.info("end executeAsync");
    }
}
