package world.ztomorrow.zttimer.utils;

import cn.hutool.core.util.IdUtil;
import org.quartz.CronExpression;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeUtils {
    public static String GetEnableLockKey(String app){
        return "enable_timer_lock:" + app;
    }

    public static String GetTimeBucketLockKey(LocalDateTime time , int bucketId) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "time_bucket_lock:" + time.format(dateTimeFormatter) + "_"  + bucketId;
    }

    public static String GetTokenStr() {
        String uuid = IdUtil.simpleUUID();
        String thread = Thread.currentThread().getName();
        return thread+uuid;
    }

    public static String GetSliceMsgKey(LocalDateTime time , int bucketId){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return time.format(dateTimeFormatter) + "_" + bucketId;
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime StringTolocalDateTime(String time) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(time, dateTimeFormatter);
    }

    public static List<Long> GetCronNextBetween(LocalDateTime startTime, LocalDateTime endTime, CronExpression cronExpression) {
        Date start = localDateTimeToDate(startTime);
        Date end = localDateTimeToDate(endTime);

        List<Long> times = new ArrayList<>();
        start = cronExpression.getNextValidTimeAfter(start);
        while (start.before(end)) {
            times.add(start.getTime());
            start = cronExpression.getNextValidTimeAfter(start);
        }
        return times;
    }

    public static List<Long> SplitTimerIDUnix(String timerIDUnix){
        List<Long> longSet = new ArrayList<>();
        String[] strList = timerIDUnix.split("_");
        if(strList.length != 2){
            return longSet;
        }
        longSet.add(Long.parseLong(strList[0]));
        longSet.add(Long.parseLong(strList[1]));
        return longSet;
    }

}
