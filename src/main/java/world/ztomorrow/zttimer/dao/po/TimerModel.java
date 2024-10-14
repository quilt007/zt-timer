package world.ztomorrow.zttimer.dao.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import world.ztomorrow.zttimer.common.BaseModel;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class TimerModel extends BaseModel implements Serializable {
    private Long timerId;

    private String app;

    private String name;

    private int status;

    private String cron;

    private String notifyHTTPParam;
}
