package world.ztomorrow.zttimer.service.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import world.ztomorrow.zttimer.common.conf.SchedulerAppConf;

import java.time.LocalDateTime;

/**
 * 调度器
 */
@Component
@Slf4j
@AllArgsConstructor
public class SchedulerWorker {

    private final SchedulerAppConf schedulerAppConf;
    private final  SchedulerTask schedulerTask;

    @Scheduled(fixedRate = 1000)
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
            schedulerTask.asyncHandleSlice(preTime, bucketId);
        } catch (Exception e) {
            log.error("[handle slice] submit nowPreMin task failed, err:",e);
        }

        try {
            schedulerTask.asyncHandleSlice(now, bucketId);
        } catch (Exception e) {
            log.error("[handle slice] submit now task failed, err:",e);
        }
    }

}
