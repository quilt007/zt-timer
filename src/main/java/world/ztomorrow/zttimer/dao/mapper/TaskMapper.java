package world.ztomorrow.zttimer.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import world.ztomorrow.zttimer.dao.po.TaskModel;

import java.util.List;

@Mapper
public interface TaskMapper {
    void batchSave(@Param("taskList") List<TaskModel> taskList);
}
