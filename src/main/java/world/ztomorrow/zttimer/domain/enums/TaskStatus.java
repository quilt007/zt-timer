package world.ztomorrow.zttimer.domain.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    NotRun(0),
    Running(1),
    Succeed(2),
    Failed(3);

    TaskStatus(int status) {
        this.status = status;
    }
    private final int status;

}
