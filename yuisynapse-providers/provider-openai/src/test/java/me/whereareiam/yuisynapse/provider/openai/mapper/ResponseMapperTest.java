package me.whereareiam.yuisynapse.provider.openai.mapper;

import me.whereareiam.yuisynapse.model.StreamChunk;
import me.whereareiam.yuisynapse.model.SynapseResponse;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIMessage;
import me.whereareiam.yuisynapse.provider.openai.model.OpenAIResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseMapperTest {
    
    @Test
    void shouldMapOpenAiResponseToSynapse() {
        // Given
        OpenAIMessage message = new OpenAIMessage();
        message.setRole("assistant");
        message.setContent("Hello! How can I help you today?");
        
        OpenAIResponse.Choice choice = new OpenAIResponse.Choice();
        choice.setIndex(0);
        choice.setMessage(message);
        choice.setFinish_reason("stop");
        
        OpenAIResponse.Usage usage = new OpenAIResponse.Usage();
        usage.setPrompt_tokens(15);
        usage.setCompletion_tokens(10);
        usage.setTotal_tokens(25);
        
        OpenAIResponse openAiResponse = new OpenAIResponse();
        openAiResponse.setId("chatcmpl-123");
        openAiResponse.setModel("gpt-4");
        openAiResponse.setChoices(List.of(choice));
        openAiResponse.setUsage(usage);
        
        // When
        SynapseResponse synapseResponse = ResponseMapper.toSynapse(openAiResponse);
        
        // Then
        assertThat(synapseResponse.getContent()).isEqualTo("Hello! How can I help you today?");
        assertThat(synapseResponse.getTokensUsed()).isEqualTo(25);
        assertThat(synapseResponse.getModel()).isEqualTo("gpt-4");
        assertThat(synapseResponse.isStreaming()).isFalse();
    }
    
    @Test
    void shouldIncludeMetadataInResponse() {
        // Given
        OpenAIMessage message = new OpenAIMessage();
        message.setContent("Response");
        
        OpenAIResponse.Choice choice = new OpenAIResponse.Choice();
        choice.setMessage(message);
        choice.setFinish_reason("stop");
        
        OpenAIResponse openAiResponse = new OpenAIResponse();
        openAiResponse.setId("chatcmpl-456");
        openAiResponse.setChoices(List.of(choice));
        
        // When
        SynapseResponse synapseResponse = ResponseMapper.toSynapse(openAiResponse);
        
        // Then
        Map<String, Object> metadata = synapseResponse.getProviderMetadata();
        assertThat(metadata).containsEntry("openai_id", "chatcmpl-456");
        assertThat(metadata).containsEntry("finish_reason", "stop");
    }
    
    @Test
    void shouldHandleEmptyChoices() {
        // Given
        OpenAIResponse openAiResponse = new OpenAIResponse();
        openAiResponse.setChoices(List.of());
        
        // When
        SynapseResponse synapseResponse = ResponseMapper.toSynapse(openAiResponse);
        
        // Then
        assertThat(synapseResponse.getContent()).isEmpty();
        assertThat(synapseResponse.getTokensUsed()).isZero();
    }
    
    @Test
    void shouldHandleNullChoices() {
        // Given
        OpenAIResponse openAiResponse = new OpenAIResponse();
        openAiResponse.setChoices(null);
        
        // When
        SynapseResponse synapseResponse = ResponseMapper.toSynapse(openAiResponse);
        
        // Then
        assertThat(synapseResponse.getContent()).isEmpty();
        assertThat(synapseResponse.getTokensUsed()).isZero();
    }
    
    @Test
    void shouldHandleNullMessage() {
        // Given
        OpenAIResponse.Choice choice = new OpenAIResponse.Choice();
        choice.setMessage(null);
        choice.setFinish_reason("error");
        
        OpenAIResponse openAiResponse = new OpenAIResponse();
        openAiResponse.setChoices(List.of(choice));
        
        // When
        SynapseResponse synapseResponse = ResponseMapper.toSynapse(openAiResponse);
        
        // Then
        assertThat(synapseResponse.getContent()).isEmpty();
    }
    
    @Test
    void shouldHandleNullUsage() {
        // Given
        OpenAIMessage message = new OpenAIMessage();
        message.setContent("Response without usage");
        
        OpenAIResponse.Choice choice = new OpenAIResponse.Choice();
        choice.setMessage(message);
        
        OpenAIResponse openAiResponse = new OpenAIResponse();
        openAiResponse.setChoices(List.of(choice));
        openAiResponse.setUsage(null);
        
        // When
        SynapseResponse synapseResponse = ResponseMapper.toSynapse(openAiResponse);
        
        // Then
        assertThat(synapseResponse.getContent()).isEqualTo("Response without usage");
        assertThat(synapseResponse.getTokensUsed()).isZero();
    }
    
    @Test
    void shouldMapStreamChunk() {
        // Given
        String delta = "Hello ";
        int index = 0;
        boolean isComplete = false;
        
        // When
        StreamChunk chunk = ResponseMapper.toStreamChunk(delta, index, isComplete);
        
        // Then
        assertThat(chunk.getContent()).isEqualTo("Hello ");
        assertThat(chunk.getDelta()).isEqualTo("Hello ");
        assertThat(chunk.getIndex()).isEqualTo(0);
        assertThat(chunk.isComplete()).isFalse();
    }
    
    @Test
    void shouldMapCompleteStreamChunk() {
        // Given
        String delta = "";
        int index = 10;
        boolean isComplete = true;
        
        // When
        StreamChunk chunk = ResponseMapper.toStreamChunk(delta, index, isComplete);
        
        // Then
        assertThat(chunk.getContent()).isEmpty();
        assertThat(chunk.isComplete()).isTrue();
    }
    
    @Test
    void shouldHandleMultipleChoices() {
        // Given - OpenAI can return multiple choices
        OpenAIMessage message1 = new OpenAIMessage();
        message1.setContent("First response");
        
        OpenAIMessage message2 = new OpenAIMessage();
        message2.setContent("Second response");
        
        OpenAIResponse.Choice choice1 = new OpenAIResponse.Choice();
        choice1.setMessage(message1);
        choice1.setFinish_reason("stop");
        
        OpenAIResponse.Choice choice2 = new OpenAIResponse.Choice();
        choice2.setMessage(message2);
        choice2.setFinish_reason("stop");
        
        OpenAIResponse openAiResponse = new OpenAIResponse();
        openAiResponse.setChoices(List.of(choice1, choice2));
        
        // When - should use first choice
        SynapseResponse synapseResponse = ResponseMapper.toSynapse(openAiResponse);
        
        // Then
        assertThat(synapseResponse.getContent()).isEqualTo("First response");
    }
}
