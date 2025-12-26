package me.whereareiam.yuisynapse.provider.openai.model;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIStreamResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<StreamChoice> choices;
    
    @Data
    public static class StreamChoice {
        private int index;
        private Delta delta;
        private String finish_reason;
    }
    
    @Data
    public static class Delta {
        private String role;
        private String content;
    }
}
