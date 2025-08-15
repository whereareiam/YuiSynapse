package me.whereareiam.yuisynapse.provider.openrouter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenRouterChatRequest {
    private String model;
    private List<OpenRouterMessage> messages;
    private Double temperature;
    private Integer max_tokens;
    private Boolean stream;
}


