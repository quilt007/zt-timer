package world.ztomorrow.zttimer.common.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class SchedulerAppConf {

    /**
     * 桶数量
     */
    @Value("${scheduler.bucketsNum}")
    private int bucketsNum;
    /**
     *  调度时，分布式锁过期时长
     */
    @Value("${scheduler.tryLockSeconds}")
    private int tryLockSeconds;
    @Value("${scheduler.tryLockGapMilliSeconds}")
    private int tryLockGapMilliSeconds;
    /**
     *  调度成功，延迟锁过期时间
     */
    @Value("${scheduler.successExpireSeconds}")
    private int successExpireSeconds;

    @Value("${scheduler.pool.corePoolSize}")
    private int corePoolSize;

    @Value("${scheduler.pool.maxPoolSize}")
    private int maxPoolSize;

    @Value("${scheduler.pool.queueCapacity}")
    private int queueCapacity;

    @Value("${scheduler.pool.namePrefix}")
    private String namePrefix;
}
