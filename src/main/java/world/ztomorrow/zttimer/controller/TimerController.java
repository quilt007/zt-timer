package world.ztomorrow.zttimer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import world.ztomorrow.zttimer.common.ResponseEntity;
import world.ztomorrow.zttimer.domain.dto.TimerDTO;
import world.ztomorrow.zttimer.service.TimerService;

@Slf4j
@RestController
@RequestMapping("/xtimer")
@RequiredArgsConstructor
public class TimerController {

    private final TimerService timerService;

    /**
     * 创建定时器
     */
    @PostMapping(value = "/createTimer")
    public ResponseEntity<Long> createTimer(@RequestBody TimerDTO timerDTO) {
        return ResponseEntity.ok(timerService.createTimer(timerDTO));
    }

    /**
     * 激活定时器
     */
    @GetMapping(value = "/enableTimer")
    public ResponseEntity<String> enableTimer(@RequestParam(value = "app") String app,
                                              @RequestParam(value = "timerId") Long timerId,
                                              @RequestHeader MultiValueMap<String, String> headers){
        timerService.enableTimer(app, timerId);
        return ResponseEntity.ok();
    }

    @PostMapping("/callback")
    public ResponseEntity<String> callback(@RequestBody String callbackInfo) {
        log.info("CALLBACK:"+callbackInfo);
        // 消息队列发送消息
        return ResponseEntity.ok(
                "ok"
        );
    }
}
