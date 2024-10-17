package world.ztomorrow.zttimer;

import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

//@SpringBootTest
class ZtTimerApplicationTests {

    @Test
    void contextLoads() throws ParseException {
        LocalDateTime start =  LocalDateTime.now();
        Date from = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
        CronExpression cronExpression = new CronExpression("0 28 10 * * ? ");
        Date next = cronExpression.getNextValidTimeAfter(from);
        System.out.println(LocalDateTime.ofInstant(next.toInstant(), ZoneId.systemDefault()));
    }

    @Test
    void test() {

        Date nowPreMin = new Date(1729167340000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(nowPreMin);
        System.out.println(timeStr);
    }

    @Test
    void test2() {
    }
}
