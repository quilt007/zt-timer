package world.ztomorrow.zttimer.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import world.ztomorrow.zttimer.common.conf.TriggerAppConf;
import world.ztomorrow.zttimer.dao.mapper.TaskMapper;
import world.ztomorrow.zttimer.dao.po.TaskModel;
import world.ztomorrow.zttimer.domain.enums.TaskStatus;
import world.ztomorrow.zttimer.service.trigger.TriggerPoolTask;
import world.ztomorrow.zttimer.utils.TimeUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class TriggerManager extends TimerTask {

    TriggerAppConf triggerAppConf;

    TriggerPoolTask triggerPoolTask;

    RedisManager redisManager;

    TaskMapper taskMapper;

    private final CountDownLatch latch ;
    private Long count = 0L;

    private final LocalDateTime startTime;

    private final LocalDateTime endTime;

    private final String minuteBucketKey;

    public TriggerManager(TriggerAppConf triggerAppConf,TriggerPoolTask triggerPoolTask,
                          RedisManager redisManager,TaskMapper taskMapper,CountDownLatch latch,
                            LocalDateTime startTime, LocalDateTime endTime, String minuteBucketKey) {
        this.triggerAppConf = triggerAppConf;
        this.triggerPoolTask = triggerPoolTask;
        this.redisManager = redisManager;
        this.taskMapper = taskMapper;
        this.latch = latch;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minuteBucketKey = minuteBucketKey;
    }

    @Override
    public void run() {
        // 在开始时间与结束时间内，循环触发定时任务
        LocalDateTime start = startTime.plusSeconds( count * triggerAppConf.getZrangeGapSeconds());
        if (start.isAfter(endTime)) {
            latch.countDown();
            return;
        }

        try {
            this.handleBatch(start, startTime.plusSeconds((triggerAppConf.getZrangeGapSeconds())));
        } catch (Exception e) {
            log.error("handleBatch Error. minuteBucketKey{},tStartTime:{},e:", minuteBucketKey, startTime, e);
        }
        count++;
    }

    private void handleBatch(LocalDateTime start, LocalDateTime end) {
        //
        List<TaskModel> tasks = getTasksByTime(start, end);
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        for (TaskModel task : tasks) {
            try {
                if (task == null) {
                    continue;
                }
                triggerPoolTask.runExecutor(task);
            } catch (Exception e) {
                log.error("executor run task error,task{}", task);
            }
        }
    }

    private List<TaskModel> getTasksByTime(LocalDateTime start, LocalDateTime end) {
        List<TaskModel> tasks = new ArrayList<>();

        long startDate = TimeUtils.localDateTimeToDate(start).getTime();
        long endDate = TimeUtils.localDateTimeToDate(end).getTime();
        // 先走缓存
        try {
            tasks = redisManager.getTasksFromCache(minuteBucketKey, startDate, endDate);
        } catch (Exception e) {
            log.error("getTasksFromCache error: ", e);
            // 缓存miss,走数据库
            try {
                tasks = taskMapper.getTasksByTimeRange(startDate, endDate - 1, TaskStatus.NotRun.getStatus());
            } catch (Exception e1) {
                log.error("getTasksByConditions error: ", e1);
            }
        }
        return tasks;
    }


}
