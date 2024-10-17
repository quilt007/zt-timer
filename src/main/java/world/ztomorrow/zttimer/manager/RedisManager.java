package world.ztomorrow.zttimer.manager;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import world.ztomorrow.zttimer.common.conf.SchedulerAppConf;
import world.ztomorrow.zttimer.dao.po.TaskModel;
import world.ztomorrow.zttimer.domain.enums.ErrorCode;
import world.ztomorrow.zttimer.exception.BusinessException;
import world.ztomorrow.zttimer.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisManager {

    private final StringRedisTemplate stringRedisTemplate;
    private final SchedulerAppConf schedulerAppConf;
    private final RedisTemplate<String, Object> redisTemplate;

    public boolean lock(String key, String token, long expireSeconds) {
        // 首先查询锁是否属于自己
        Object res = stringRedisTemplate.opsForValue().get(key);
        if (res != null && StrUtil.equals(res.toString(), token)) {
            return true;
        }

        // 不属于自己，尝试获取锁
        boolean ok = Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue().setIfAbsent(
                        key, token, expireSeconds, TimeUnit.SECONDS)
        );
        if (!ok) {
            log.info("lock is acquired by others");
        }
        return ok;
    }


    public void expireLock(String key, String token, long expireSeconds) {
        Long execute  = redisTemplate.execute(getExpireLockScript(), Collections.singletonList(key), token, expireSeconds);
        if (execute.longValue() == 0) {
            log.info("延期{}失败:{}", key, execute);
        } else if (execute.longValue() == 1) {
            log.info("延期{}成功:{}", key, execute);
        }
    }

    private DefaultRedisScript<Long> getExpireLockScript() {
        String script = "local lockerKey = KEYS[1]\n" +
                "  local targetToken = ARGV[1]\n" +
                "  local duration = ARGV[2]\n" +
                "  local getToken = redis.call('get',lockerKey)\n" +
                "  if (not getToken or getToken ~= targetToken) then\n" +
                "    return 0\n" +
                "\telse\n" +
                "\t\treturn redis.call('expire',lockerKey,duration)\n" +
                "  end";
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(Long.class);
        defaultRedisScript.setScriptText(script);
        return defaultRedisScript;
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

    public String GetTableName(TaskModel taskModel) {
        int bucketsNum = schedulerAppConf.getBucketsNum();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(new Date(taskModel.getRunTimer()));
        long index = taskModel.getTimerId() % bucketsNum;
        return timeStr + "_" + index;
    }

    public List<TaskModel> getTasksFromCache(String key, long start, long end) {
        List<TaskModel> tasks = new ArrayList<>();

        Set<String> timerIDUnixSet = stringRedisTemplate.opsForZSet().rangeByScore(key, start, end - 1);
        if (CollectionUtils.isEmpty(timerIDUnixSet)) {
            return tasks;
        }

        for (String timerIDUnix : timerIDUnixSet) {
            TaskModel task = new TaskModel();
            List<Long> longSet = TimeUtils.SplitTimerIDUnix(timerIDUnix);
            if (longSet.size() != 2) {
                log.error("splitTimerIDUnix 错误, timerIDUnix:{}", timerIDUnix);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "splitTimerIDUnix 错误, timerIDUnix:" + timerIDUnix);
            }
            task.setTimerId(longSet.get(0));
            task.setRunTimer(longSet.get(1));
            tasks.add(task);
        }

        return tasks;
    }
}
