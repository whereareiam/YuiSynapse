package me.whereareiam.yuisynapse.provider.openrouter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenRouterChoice {
    private int index;
    private OpenRouterMessage message;
    private String finish_reason;
}


