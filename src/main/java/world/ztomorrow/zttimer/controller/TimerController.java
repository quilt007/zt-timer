package world.ztomorrow.zttimer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import world.ztomorrow.zttimer.common.ResponseEntity;
import world.ztomorrow.zttimer.domain.dto.TimerDTO;

@RestController
@RequestMapping("/xtimer")
@RequiredArgsConstructor
public class TimerController {

    /**
     * 创建定时任务
     */
    @PostMapping(value = "/createTimer")
    public ResponseEntity<Long> createTimer(@RequestBody TimerDTO timerDTO) {
        return ResponseEntity.ok();
    }

    /**
     * 激活定时服务
     */
    @GetMapping(value = "/enableTimer")
    public ResponseEntity<String> enableTimer(@RequestParam(value = "app") String app,
                                              @RequestParam(value = "timerId") Long timerId,
                                              @RequestHeader MultiValueMap<String, String> headers){
        return ResponseEntity.ok();

    }
}
