package world.ztomorrow.zttimer.manager;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import world.ztomorrow.zttimer.common.conf.MigratorAppConf;
import world.ztomorrow.zttimer.dao.mapper.TaskMapper;
import world.ztomorrow.zttimer.dao.po.TaskModel;
import world.ztomorrow.zttimer.dao.po.TimerModel;
import world.ztomorrow.zttimer.domain.enums.ErrorCode;
import world.ztomorrow.zttimer.domain.enums.TaskStatus;
import world.ztomorrow.zttimer.domain.enums.TimerStatus;
import world.ztomorrow.zttimer.exception.BusinessException;
import world.ztomorrow.zttimer.utils.TimeUtils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MigratorManager {

    private final MigratorAppConf migratorAppConf;
    private final TaskMapper taskMapper;
    private final RedisManager redisManager;
    /**
     *  将定时器将来要执行的任务，迁移至redis
     */
    public void migrateTimer(TimerModel timerModel) {
        // 定时器必须存在
        if(timerModel == null) {
            return;
        }
        // 激活的定时器在可以迁移数据
        if(timerModel.getStatus() != TimerStatus.Enable.getStatus()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Timer非Enable状态，迁移失败，timerId:"+timerModel.getTimerId());
        }
        // 校验cron表达式
        CronExpression cronExpression;
        try {
            cronExpression = new CronExpression(timerModel.getCron());
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析cron表达式失败："+timerModel.getCron());
        }
        // 获取迁移数据的时间区间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusMinutes(migratorAppConf.getMigrateStepMinutes() * 2L);
        List<Long> executeTimes = TimeUtils.GetCronNextBetween(now, end, cronExpression);
        if (CollectionUtils.isEmpty(executeTimes) ){
            log.warn("获取执行时机 executeTimes 为空");
            return;
        }
        // 在时间区间内，需要执行任务点录入数据库
        List<TaskModel> taskList = executeTimes.stream().map(time -> {
            TaskModel task = BeanUtil.copyProperties(timerModel, TaskModel.class);
            task.setRunTimer(time);
            task.setStatus(TaskStatus.NotRun.getStatus());
            return task;
        }).toList();
        taskMapper.batchSave(taskList);
        // 同时放入redis
        boolean cacheRes = redisManager.cacheSaveTasks(taskList);
        if(!cacheRes){
            log.error("ZSet存储taskList失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ZSet存储taskList失败，timerId:"+timerModel.getTimerId());
        }
    }
}
