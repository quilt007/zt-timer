package world.ztomorrow.zttimer.domain.dto;

import lombok.Data;

import java.util.Map;

@Data
public class NotifyHTTPParam {
    private String method;
    private String url;
    private Map<String,String> header;
    private String body;
}
