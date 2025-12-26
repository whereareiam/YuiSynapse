package me.whereareiam.yuisynapse.provider.openai.mapper;

import me.whereareiam.yuisynapse.model.SynapseMessage;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIMessage;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIRequest;
import me.whereareiam.yuisynapse.request.SynapseConversationRequest;
import me.whereareiam.yuisynapse.request.SynapseSingleMessageRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestMapper {
    public static OpenAIRequest toOpenAI(SynapseSingleMessageRequest request) {
        OpenAIRequest openAiRequest = new OpenAIRequest();
        openAiRequest.setModel(request.getModel());
        openAiRequest.setTemperature(request.getTemperature());
        openAiRequest.setMax_tokens(request.getMaxTokens());
        openAiRequest.setStream(false);
        
        List<OpenAIMessage> messages = new ArrayList<>();
        
        // Add system message
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            OpenAIMessage systemMessage = new OpenAIMessage();
            systemMessage.setRole("system");
            systemMessage.setContent(request.getSystemPrompt());
            messages.add(systemMessage);
        }
        
        // Add user message
        OpenAIMessage userMessage = new OpenAIMessage();
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        messages.add(userMessage);
        
        openAiRequest.setMessages(messages);
        return openAiRequest;
    }
    
    public static OpenAIRequest toOpenAI(SynapseConversationRequest request) {
        OpenAIRequest openAiRequest = new OpenAIRequest();
        openAiRequest.setModel(request.getModel());
        openAiRequest.setTemperature(request.getTemperature());
        openAiRequest.setMax_tokens(request.getMaxTokens());
        openAiRequest.setStream(request.isStream());
        
        List<OpenAIMessage> messages = new ArrayList<>();
        
        // Add system message
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            OpenAIMessage systemMessage = new OpenAIMessage();
            systemMessage.setRole("system");
            systemMessage.setContent(request.getSystemPrompt());
            messages.add(systemMessage);
        }
        
        // Add conversation history
        if (request.getHistory() != null) {
            for (SynapseMessage synapseMessage : request.getHistory()) {
                OpenAIMessage openAiMessage = new OpenAIMessage();
                openAiMessage.setRole(synapseMessage.getRole());
                openAiMessage.setContent(synapseMessage.getContent());
                messages.add(openAiMessage);
            }
        }
        
        openAiRequest.setMessages(messages);
        return openAiRequest;
    }
}
