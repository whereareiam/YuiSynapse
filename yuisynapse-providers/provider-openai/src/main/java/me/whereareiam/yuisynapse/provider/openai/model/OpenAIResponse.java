package me.whereareiam.yuisynapse.provider.openai.model;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    
    @Data
    public static class Choice {
        private int index;
        private OpenAIMessage message;
        private String finish_reason;
    }
    
    @Data
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }
}
