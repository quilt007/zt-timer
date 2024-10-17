package world.ztomorrow.zttimer.service.migrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import world.ztomorrow.zttimer.common.conf.MigratorAppConf;
import world.ztomorrow.zttimer.dao.mapper.TimerMapper;
import world.ztomorrow.zttimer.dao.po.TimerModel;
import world.ztomorrow.zttimer.domain.enums.TimerStatus;
import world.ztomorrow.zttimer.manager.MigratorManager;
import world.ztomorrow.zttimer.manager.RedisManager;
import world.ztomorrow.zttimer.utils.TimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MigratorWorker {

    private final TimerMapper timerMapper;

    private final MigratorAppConf migratorAppConf;

    private final MigratorManager migratorManager;

    private final RedisManager redisManager;

    @Scheduled(fixedRate = 10 * 1000) // 60*60*1000 一小时执行一次
    public void work() {
        log.info("开始迁移时间：{}", LocalDateTime.now());
        Date startHour = getStartHour(new Date());
        String lockToken = TimeUtils.GetTokenStr();
        boolean ok = redisManager.lock(
                TimeUtils.GetMigratorLockKey(startHour),
                lockToken,
                60L * migratorAppConf.getMigrateTryLockMinutes());
        if (!ok) {
            log.warn("migrator get lock failed！{}", TimeUtils.GetMigratorLockKey(startHour));
            return;
        }

        //迁移
        migrate();

        // 更新分布式锁过期时间
        redisManager.expireLock(
                TimeUtils.GetMigratorLockKey(startHour),
                lockToken,
                60L * migratorAppConf.getMigrateSuccessExpireMinutes());
    }

    private Date getStartHour(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        try {
            return sdf.parse(sdf.format(date));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private void migrate() {
        List<TimerModel> timers = timerMapper.getTimersByStatus(TimerStatus.Enable.getStatus());
        if (CollectionUtils.isEmpty(timers)) {
            log.info("migrate timers is empty");
            return;
        }

        for (TimerModel timerModel : timers) {
            migratorManager.migrateTimer(timerModel);
        }
    }
}

