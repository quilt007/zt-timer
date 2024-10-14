package world.ztomorrow.zttimer.dao.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import world.ztomorrow.zttimer.common.BaseModel;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskModel extends BaseModel implements Serializable {
    private Integer taskId;

    private String app;

    private Long timerId;

    private String output;

    private Long runTimer;

    private int costTime;

    private int status;
}
