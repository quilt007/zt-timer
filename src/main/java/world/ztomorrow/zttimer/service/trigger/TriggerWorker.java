package world.ztomorrow.zttimer.service.trigger;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import world.ztomorrow.zttimer.common.conf.TriggerAppConf;
import world.ztomorrow.zttimer.dao.mapper.TaskMapper;
import world.ztomorrow.zttimer.manager.TriggerManager;
import world.ztomorrow.zttimer.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
@AllArgsConstructor
public class TriggerWorker {

    private final TriggerAppConf triggerAppConf;
    private final TaskMapper taskMapper;

    public void work(String minuteBucketKey) {
        LocalDateTime startMinute = getStartMinute(minuteBucketKey);
        LocalDateTime endMinute = startMinute.plusMinutes(1L);

        CountDownLatch latch = new CountDownLatch(1);
        Timer timer = new Timer();
        TriggerManager task = new TriggerManager(
                triggerAppConf, taskMapper, startMinute, endMinute, latch, minuteBucketKey
        );
        timer.scheduleAtFixedRate(task, 0L, triggerAppConf.getZrangeGapSeconds() * 1000L);

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("执行TriggerTimerTask异常中断，task:{}", task);
        } finally {
            timer.cancel();
        }
    }

    private LocalDateTime getStartMinute(String minuteBucketKey){
        String[] timeBucket = minuteBucketKey.split("_");
        if(timeBucket.length != 2){
            log.error("TriggerWorker getStartMinute 错误");
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        LocalDateTime startMinute = null;
        startMinute = TimeUtils.StringTolocalDateTime(timeBucket[0]);
        return startMinute;
    }
}
