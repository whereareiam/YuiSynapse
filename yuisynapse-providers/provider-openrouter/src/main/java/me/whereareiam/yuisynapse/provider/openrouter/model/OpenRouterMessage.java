package me.whereareiam.yuisynapse.provider.openrouter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenRouterMessage {
    private String role;
    private String content;
}


