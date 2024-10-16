package world.ztomorrow.zttimer.service;

import world.ztomorrow.zttimer.domain.dto.TimerDTO;

public interface TimerService {
    Long createTimer(TimerDTO timerDTO);

    void enableTimer(String app, Long timerId);
}
