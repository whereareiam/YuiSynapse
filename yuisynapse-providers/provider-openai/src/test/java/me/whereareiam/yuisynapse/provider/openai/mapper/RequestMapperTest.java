package me.whereareiam.yuisynapse.provider.openai.mapper;

import me.whereareiam.yuisynapse.model.SynapseMessage;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIMessage;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIRequest;
import me.whereareiam.yuisynapse.request.SynapseConversationRequest;
import me.whereareiam.yuisynapse.request.SynapseSingleMessageRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMapperTest {
    
    @Test
    void shouldMapSingleMessageRequest() {
        // Given
        SynapseSingleMessageRequest synapseRequest = SynapseSingleMessageRequest.builder()
                .model("gpt-4")
                .temperature(0.7)
                .maxTokens(150)
                .systemPrompt("You are a helpful assistant")
                .message("Hello, how are you?")
                .build();
        
        // When
        OpenAIRequest openAiRequest = RequestMapper.toOpenAI(synapseRequest);
        
        // Then
        assertThat(openAiRequest.getModel()).isEqualTo("gpt-4");
        assertThat(openAiRequest.getTemperature()).isEqualTo(0.7);
        assertThat(openAiRequest.getMax_tokens()).isEqualTo(150);
        assertThat(openAiRequest.getStream()).isFalse();
        assertThat(openAiRequest.getMessages()).hasSize(2);
    }
    
    @Test
    void shouldIncludeSystemPromptInSingleMessage() {
        // Given
        SynapseSingleMessageRequest synapseRequest = SynapseSingleMessageRequest.builder()
                .model("gpt-3.5-turbo")
                .systemPrompt("You are a moderation bot")
                .message("Is this message appropriate?")
                .build();
        
        // When
        OpenAIRequest openAiRequest = RequestMapper.toOpenAI(synapseRequest);
        
        // Then
        List<OpenAIMessage> messages = openAiRequest.getMessages();
        assertThat(messages.get(0).getRole()).isEqualTo("system");
        assertThat(messages.get(0).getContent()).isEqualTo("You are a moderation bot");
        assertThat(messages.get(1).getRole()).isEqualTo("user");
        assertThat(messages.get(1).getContent()).isEqualTo("Is this message appropriate?");
    }
    
    @Test
    void shouldHandleMissingSystemPromptInSingleMessage() {
        // Given
        SynapseSingleMessageRequest synapseRequest = SynapseSingleMessageRequest.builder()
                .model("gpt-4")
                .message("Hello")
                .build();
        
        // When
        OpenAIRequest openAiRequest = RequestMapper.toOpenAI(synapseRequest);
        
        // Then
        assertThat(openAiRequest.getMessages()).hasSize(1);
        assertThat(openAiRequest.getMessages().getFirst().getRole()).isEqualTo("user");
    }
    
    @Test
    void shouldMapConversationRequest() {
        // Given
        List<SynapseMessage> history = List.of(
                SynapseMessage.builder()
                        .role("user")
                        .content("Hello")
                        .build(),
                SynapseMessage.builder()
                        .role("assistant")
                        .content("Hi there!")
                        .build(),
                SynapseMessage.builder()
                        .role("user")
                        .content("How are you?")
                        .build()
        );
        
        SynapseConversationRequest synapseRequest = SynapseConversationRequest.builder()
                .model("gpt-4")
                .temperature(0.8)
                .maxTokens(200)
                .systemPrompt("You are a chat bot")
                .history(history)
                .stream(false)
                .build();
        
        // When
        OpenAIRequest openAiRequest = RequestMapper.toOpenAI(synapseRequest);
        
        // Then
        assertThat(openAiRequest.getModel()).isEqualTo("gpt-4");
        assertThat(openAiRequest.getTemperature()).isEqualTo(0.8);
        assertThat(openAiRequest.getMax_tokens()).isEqualTo(200);
        assertThat(openAiRequest.getStream()).isFalse();
        assertThat(openAiRequest.getMessages()).hasSize(4); // 1 system + 3 history
    }
    
    @Test
    void shouldPreserveMessageOrderInConversation() {
        // Given
        List<SynapseMessage> history = List.of(
                SynapseMessage.builder().role("user").content("Message 1").build(),
                SynapseMessage.builder().role("assistant").content("Response 1").build(),
                SynapseMessage.builder().role("user").content("Message 2").build()
        );
        
        SynapseConversationRequest synapseRequest = SynapseConversationRequest.builder()
                .model("gpt-3.5-turbo")
                .systemPrompt("System")
                .history(history)
                .build();
        
        // When
        OpenAIRequest openAiRequest = RequestMapper.toOpenAI(synapseRequest);
        
        // Then
        List<OpenAIMessage> messages = openAiRequest.getMessages();
        assertThat(messages.get(0).getRole()).isEqualTo("system");
        assertThat(messages.get(1).getRole()).isEqualTo("user");
        assertThat(messages.get(1).getContent()).isEqualTo("Message 1");
        assertThat(messages.get(2).getRole()).isEqualTo("assistant");
        assertThat(messages.get(2).getContent()).isEqualTo("Response 1");
        assertThat(messages.get(3).getRole()).isEqualTo("user");
        assertThat(messages.get(3).getContent()).isEqualTo("Message 2");
    }
    
    @Test
    void shouldHandleEmptyHistoryInConversation() {
        // Given
        SynapseConversationRequest synapseRequest = SynapseConversationRequest.builder()
                .model("gpt-4")
                .systemPrompt("You are helpful")
                .history(List.of())
                .build();
        
        // When
        OpenAIRequest openAiRequest = RequestMapper.toOpenAI(synapseRequest);
        
        // Then
        assertThat(openAiRequest.getMessages()).hasSize(1); // Only system message
        assertThat(openAiRequest.getMessages().getFirst().getRole()).isEqualTo("system");
    }
    
    @Test
    void shouldSetStreamFlagCorrectly() {
        // Given
        SynapseConversationRequest streamingRequest = SynapseConversationRequest.builder()
                .model("gpt-4")
                .stream(true)
                .build();
        
        SynapseConversationRequest nonStreamingRequest = SynapseConversationRequest.builder()
                .model("gpt-4")
                .stream(false)
                .build();
        
        // When
        OpenAIRequest streamingOpenAi = RequestMapper.toOpenAI(streamingRequest);
        OpenAIRequest nonStreamingOpenAi = RequestMapper.toOpenAI(nonStreamingRequest);
        
        // Then
        assertThat(streamingOpenAi.getStream()).isTrue();
        assertThat(nonStreamingOpenAi.getStream()).isFalse();
    }
}
