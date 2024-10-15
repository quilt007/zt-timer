package world.ztomorrow.zttimer.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import world.ztomorrow.zttimer.dao.po.TimerModel;

@Mapper
public interface TimerMapper {

    void save(@Param("timerModel") TimerModel timerModel);
}
