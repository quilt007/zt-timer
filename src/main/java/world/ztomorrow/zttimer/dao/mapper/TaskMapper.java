package world.ztomorrow.zttimer.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import world.ztomorrow.zttimer.dao.po.TaskModel;

import java.util.List;

@Mapper
public interface TaskMapper {
    void batchSave(@Param("taskList") List<TaskModel> taskList);

    List<TaskModel> getTasksByTimeRange(@Param("startTime") long startTime, @Param("endTime") long endTime,@Param("taskStatus") int status);

    TaskModel getTasksByTimerIdUnix(@Param("timerId") Long timerId, @Param("runTimer") Long runTimer);

    void update(TaskModel taskModel);
}
