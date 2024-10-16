package world.ztomorrow.zttimer.domain.enums;

import lombok.Getter;

@Getter
public enum TimerStatus {
    Unable(1),
    Enable(2),;

    TimerStatus(int status) {
        this.status = status;
    }
    private final int status;

    public static TimerStatus getTimerStatus(int status){
        for (TimerStatus value:TimerStatus.values()) {
            if(value.status == status){
                return value;
            }
        }
        return null;
    }
}
