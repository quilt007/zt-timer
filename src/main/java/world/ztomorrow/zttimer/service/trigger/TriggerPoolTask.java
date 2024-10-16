package world.ztomorrow.zttimer.service.trigger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import world.ztomorrow.zttimer.dao.po.TaskModel;
import world.ztomorrow.zttimer.service.executor.ExecutorWorker;

@Slf4j
@Component
public class TriggerPoolTask {

    @Autowired
    ExecutorWorker executorWorker;

    @Async("triggerPool")
    public void runExecutor(TaskModel task) {
        if(task == null){
            return;
        }
        log.info("start runExecutor");

        executorWorker.work(task.getTimerId(), task.getRunTimer());

        log.info("end executeAsync");
    }
}
