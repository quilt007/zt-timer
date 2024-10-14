package world.ztomorrow.zttimer.common;

import lombok.Data;
import world.ztomorrow.zttimer.domain.enums.ResponseEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ResponseEntity<T> implements Serializable {
    private int code;
    private String message;
    private String datetime;
    private T data;

    public static <T> ResponseEntity<T> ok(T data) {
        ResponseEntity<T> responseEntity = new ResponseEntity<>();
        responseEntity.setData(data);
        responseEntity.setCode(ResponseEnum.OK.code());
        responseEntity.setDatetime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return responseEntity;
    }

    public static <T> ResponseEntity<T> ok() {
        ResponseEntity<T> responseEntity = new ResponseEntity<>();
        responseEntity.setCode(ResponseEnum.OK.code());
        responseEntity.setMessage(ResponseEnum.OK.message());
        return responseEntity;
    }

    public static <T> ResponseEntity<T> fail() {
        ResponseEntity<T> responseEntity = new ResponseEntity<>();
        responseEntity.setMessage(ResponseEnum.FAIL.message());
        responseEntity.setCode(ResponseEnum.FAIL.code());
        return responseEntity;
    }

    public static <T> ResponseEntity<T> fail(ResponseEnum responseEnum) {
        ResponseEntity<T> responseEntity = new ResponseEntity<>();
        responseEntity.setMessage(responseEnum.message());
        responseEntity.setCode(responseEnum.code());
        return responseEntity;
    }

    public static <T> ResponseEntity<T> fail(ResponseEnum responseEnum, T data) {
        ResponseEntity<T> responseEntity = new ResponseEntity<>();
        responseEntity.setMessage(responseEnum.message());
        responseEntity.setCode(responseEnum.code());
        responseEntity.setData(data);
        return responseEntity;
    }

    public static <T> ResponseEntity<T> failBusinessException(int code, String msg) {
        ResponseEntity<T> responseEntity = new ResponseEntity<>();
        responseEntity.setMessage(msg);
        responseEntity.setCode(code);
        return responseEntity;
    }


    /**
     * 前端显示失败消息
     * @param msg 失败消息
     * @return
     */
    public static <T> ResponseEntity<T> showFailMsg(String msg) {
        ResponseEntity<T> serverResponseEntity = new ResponseEntity<>();
        serverResponseEntity.setMessage(msg);
        serverResponseEntity.setCode(ResponseEnum.SHOW_FAIL.code());
        return serverResponseEntity;
    }
}
