package me.whereareiam.yuisynapse.provider.openai.model;

import lombok.Data;

@Data
public class OpenAIMessage {
    private String role;
    private String content;
}
