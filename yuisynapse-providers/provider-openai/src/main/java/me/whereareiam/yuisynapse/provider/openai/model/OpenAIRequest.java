package me.whereareiam.yuisynapse.provider.openai.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OpenAIRequest {
    private String model;
    private List<OpenAIMessage> messages;
    private Double temperature;
    private Integer max_tokens;
    private Boolean stream;
    private Map<String, Object> additionalProperties;
}
