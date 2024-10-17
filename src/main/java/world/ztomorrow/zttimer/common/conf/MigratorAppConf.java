package world.ztomorrow.zttimer.common.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class MigratorAppConf {

    @Value("${migrator.workersNum}")
    private int workersNum;
    /**
     * 迁移分片的步长
     */
    @Value("${migrator.migrateStepMinutes}")
    private int migrateStepMinutes;
    /**
     *  迁移成功，更新分布式锁的时长
     */
    @Value("${migrator.migrateSuccessExpireMinutes}")
    private int migrateSuccessExpireMinutes;
    /**
     * 迁移前分布式锁过期时长
     */
    @Value("${migrator.migrateTryLockMinutes}")
    private int migrateTryLockMinutes;
    @Value("${migrator.timerDetailCacheMinutes}")
    private int timerDetailCacheMinutes;
}
