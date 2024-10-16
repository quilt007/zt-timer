package world.ztomorrow.zttimer.manager;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import world.ztomorrow.zttimer.common.conf.SchedulerAppConf;
import world.ztomorrow.zttimer.dao.po.TaskModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisManager {

    private final StringRedisTemplate stringRedisTemplate;
    private final SchedulerAppConf schedulerAppConf;

    public boolean lock(String key, String token, long expireSeconds){
        // 首先查询锁是否属于自己
        Object res = stringRedisTemplate.opsForValue().get(key);
        if(res != null && StrUtil.equals(res.toString(),token)){
            return true;
        }

        // 不属于自己，尝试获取锁
        boolean ok = Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue().setIfAbsent(
                        key, token, expireSeconds, TimeUnit.SECONDS)
        );
        if(!ok){
            log.info("lock is acquired by others");
        }
        return ok;
    }


    public boolean cacheSaveTasks(List<TaskModel> taskList) {
        try {
            SessionCallback sessionCallback = new SessionCallback() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    for (TaskModel task : taskList) {
                        stringRedisTemplate.opsForZSet().add(
                                GetTableName(task),
                                task.getTimerId() + "_" + task.getRunTimer(),
                                task.getRunTimer()
                        );
                    }
                    return operations.exec();
                }
            };
            stringRedisTemplate.execute(sessionCallback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String GetTableName(TaskModel taskModel){
        int bucketsNum = schedulerAppConf.getBucketsNum();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(new Date(taskModel.getRunTimer()));
        long index = taskModel.getTaskId() % bucketsNum;
        return timeStr + "_" + index;
    }
}
