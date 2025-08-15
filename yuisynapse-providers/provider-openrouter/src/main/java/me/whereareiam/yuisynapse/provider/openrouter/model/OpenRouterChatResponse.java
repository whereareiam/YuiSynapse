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
public class OpenRouterChatResponse {
    private String id;
    private List<OpenRouterChoice> choices;
}


