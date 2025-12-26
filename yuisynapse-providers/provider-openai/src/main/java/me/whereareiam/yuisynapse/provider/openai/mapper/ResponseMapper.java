package me.whereareiam.yuisynapse.provider.openai.mapper;

import me.whereareiam.yuisynapse.model.StreamChunk;
import me.whereareiam.yuisynapse.model.SynapseResponse;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIResponse;

import java.util.HashMap;
import java.util.Map;

public class ResponseMapper {
    public static SynapseResponse toSynapse(OpenAIResponse openAiResponse) {
        if (openAiResponse.getChoices() == null || openAiResponse.getChoices().isEmpty()) {
            return SynapseResponse.builder()
                .content("")
                .tokensUsed(0)
                .build();
        }
        
        OpenAIResponse.Choice firstChoice = openAiResponse.getChoices().getFirst();
        String content = firstChoice.getMessage() != null ? firstChoice.getMessage().getContent() : "";
        
        int tokensUsed = 0;
        if (openAiResponse.getUsage() != null) {
            tokensUsed = openAiResponse.getUsage().getTotal_tokens();
        }
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("openai_id", openAiResponse.getId());
        metadata.put("finish_reason", firstChoice.getFinish_reason());
        
        return SynapseResponse.builder()
            .content(content)
            .tokensUsed(tokensUsed)
            .model(openAiResponse.getModel())
            .isStreaming(false)
            .providerMetadata(metadata)
            .build();
    }
    
    public static StreamChunk toStreamChunk(String delta, int index, boolean isComplete) {
        return StreamChunk.builder()
            .content(delta)
            .delta(delta)
            .index(index)
            .isComplete(isComplete)
            .build();
    }
}
