package world.ztomorrow.zttimer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class ZtTimerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZtTimerApplication.class, args);
    }

}
