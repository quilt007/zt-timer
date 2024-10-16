package world.ztomorrow.zttimer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.ztomorrow.zttimer.dao.mapper.TimerMapper;
import world.ztomorrow.zttimer.dao.po.TimerModel;
import world.ztomorrow.zttimer.domain.dto.TimerDTO;
import world.ztomorrow.zttimer.domain.enums.ErrorCode;
import world.ztomorrow.zttimer.domain.enums.TimerStatus;
import world.ztomorrow.zttimer.exception.BusinessException;
import world.ztomorrow.zttimer.manager.MigratorManager;
import world.ztomorrow.zttimer.manager.RedisManager;
import world.ztomorrow.zttimer.service.TimerService;
import world.ztomorrow.zttimer.utils.TimeUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimerServiceImpl implements TimerService {

    private final TimerMapper timerMapper;
    private final RedisManager redisManager;
    private final MigratorManager migratorManager;

    @Override
    public Long createTimer(TimerDTO timerDTO) {
        // 校验cron表达式
        boolean validExpression = CronExpression.isValidExpression(timerDTO.getCron());
        if (!validExpression) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid cron");
        }
        // 创建timer对象
        TimerModel timerModel = BeanUtil.copyProperties(timerDTO, TimerModel.class);
        if (timerModel == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 定时器录入数据库
        timerMapper.save(timerModel);

        return timerDTO.getTimerId();
    }

    @Override
    public void enableTimer(String app, Long timerId) {
        // 获取操作定时器的分布式锁
        boolean ok = redisManager.lock(
                TimeUtils.GetEnableLockKey(app),
                TimeUtils.GetTokenStr(),
                3L
        );
        if (!ok) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"激活/去激活操作过于频繁，请稍后再试！");
        }

        // 尝试激活
        TimerServiceImpl proxy = (TimerServiceImpl) AopContext.currentProxy();
        proxy.doEnableTimer(timerId);
    }

    @Transactional
    public void doEnableTimer(long timerId){
        // 校验定时器是否存在
        TimerModel timerModel = timerMapper.selectById(timerId);
        if (timerModel == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "激活失败，timer不存在：timerId"+timerId);
        }
        // 校验定时器状态
        if(timerModel.getStatus() == TimerStatus.Enable.getStatus()){
            log.warn("Timer处于Enable状态，timerId:{}", timerModel.getTimerId());
        }
        // 将定时器设为激活状态
        timerModel.setStatus(TimerStatus.Enable.getStatus());
        timerMapper.update(timerModel);
        // 迁移数据
        migratorManager.migrateTimer(timerModel);
    }
}
