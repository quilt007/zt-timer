package world.ztomorrow.zttimer.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import world.ztomorrow.zttimer.dao.po.TimerModel;

import java.util.List;

@Mapper
public interface TimerMapper {

    void save(@Param("timerModel") TimerModel timerModel);

    TimerModel selectById(@Param("timerId") Long timerId);

    void update(@Param("timerModel") TimerModel timerModel);

    TimerModel getTimerById(@Param("timerId") Long timerId);

    List<TimerModel> getTimersByStatus(@Param("status") int status);
}
