package world.ztomorrow.zttimer.service.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import world.ztomorrow.zttimer.dao.mapper.TaskMapper;
import world.ztomorrow.zttimer.dao.mapper.TimerMapper;
import world.ztomorrow.zttimer.dao.po.TaskModel;
import world.ztomorrow.zttimer.dao.po.TimerModel;
import world.ztomorrow.zttimer.domain.dto.NotifyHTTPParam;
import world.ztomorrow.zttimer.domain.dto.TimerDTO;
import world.ztomorrow.zttimer.domain.enums.ErrorCode;
import world.ztomorrow.zttimer.domain.enums.TaskStatus;
import world.ztomorrow.zttimer.domain.enums.TimerStatus;
import world.ztomorrow.zttimer.exception.BusinessException;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutorWorker {


    private  final TaskMapper taskMapper;
    private final TimerMapper timerMapper;

    public void work(Long timerId, Long runTimer) {
        if (timerId == null || runTimer == null) {
            log.error("splitTimerIDUnix 错误");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "splitTimerIDUnix 错误");
        }
        TaskModel task = taskMapper.getTasksByTimerIdUnix(timerId, runTimer);
        if (task.getStatus() != TaskStatus.NotRun.getStatus()) {
            log.warn("重复执行任务： timerId{},runTimer:{}", timerId, runTimer);
            return;
        }

        // 执行回调
        executeAndPostProcess(task, timerId);
    }

    private void executeAndPostProcess(TaskModel taskModel, Long timerId) {
        TimerModel timerModel = timerMapper.getTimerById(timerId);
        if (timerModel == null) {
            log.error("执行回调错误，找不到对应的Timer。 timerId{}", timerId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行回调错误，找不到对应的Timer。 timerId" + timerId);
        }

        if (timerModel.getStatus() != TimerStatus.Enable.getStatus()) {
            log.warn("Timer已经处于去激活状态。 timerId{}", timerId);
            return;
        }

        // 触发时间的误差时间
        int gapTime = (int) (new Date().getTime() - taskModel.getRunTimer());
        taskModel.setCostTime(gapTime);

        // 执行http回调，通知业务放
        ResponseEntity<String> resp = null;
        try {
            resp = executeTimerCallBack(timerModel);
        } catch (Exception e) {
            log.error("执行回调失败，抛出异常e:{}", String.valueOf(e));
        }

        //后置处理，更新Timer执行结果
        if (resp == null) {
            taskModel.setStatus(TaskStatus.Failed.getStatus());
            taskModel.setOutput("resp is null");
        } else if (resp.getStatusCode().is2xxSuccessful()) {
            taskModel.setStatus(TaskStatus.Succeed.getStatus());
            taskModel.setOutput(resp.toString());
        } else {
            taskModel.setStatus(TaskStatus.Failed.getStatus());
            taskModel.setOutput(resp.toString());
        }

        taskMapper.update(taskModel);
    }

    private ResponseEntity<String> executeTimerCallBack(TimerModel timerModel) {
        TimerDTO timerDTO = BeanUtil.copyProperties(timerModel, TimerDTO.class);
        NotifyHTTPParam httpParam = JSONUtil.toBean(timerModel.getNotifyHTTPParam(),NotifyHTTPParam.class);
        timerDTO.setNotifyHTTPParam(httpParam);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> resp = null;
        switch (httpParam.getMethod()) {
            case "POST":
                resp = restTemplate.postForEntity(httpParam.getUrl(), httpParam.getBody(), String.class);
            default:
                log.error("不支持的httpMethod");
                break;
        }
        if (resp != null) {
            HttpStatusCode statusCode = resp.getStatusCode();
            if (!statusCode.is2xxSuccessful()) {
                log.error("http 回调失败：{}", resp);
            }
        }
        return resp;
    }
}
