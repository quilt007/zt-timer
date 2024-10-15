package world.ztomorrow.zttimer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import world.ztomorrow.zttimer.dao.mapper.TimerMapper;
import world.ztomorrow.zttimer.dao.po.TimerModel;
import world.ztomorrow.zttimer.domain.dto.TimerDTO;
import world.ztomorrow.zttimer.domain.enums.ErrorCode;
import world.ztomorrow.zttimer.exception.BusinessException;
import world.ztomorrow.zttimer.service.TimerService;

@Service
@RequiredArgsConstructor
public class TimerServiceImpl implements TimerService {

    private final TimerMapper timerMapper;

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
        // 预约定时服务录入数据库
        timerMapper.save(timerModel);

        return timerDTO.getTimerId();
    }
}
