package me.whereareiam.yuisynapse.provider.openrouter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.api.exception.ProviderRequestException;
import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.Options;
import me.whereareiam.yuisynapse.api.output.ProviderClient;
import me.whereareiam.yuisynapse.api.type.ProviderType;
import me.whereareiam.yuisynapse.provider.openrouter.model.OpenRouterChatRequest;
import me.whereareiam.yuisynapse.provider.openrouter.model.OpenRouterChatResponse;
import me.whereareiam.yuisynapse.provider.openrouter.model.OpenRouterMessage;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRouterClient implements ProviderClient {
	private final WebClient openRouterWebClient;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public ProviderType type() {
		return ProviderType.OPENROUTER;
	}

	@Override
	public Flux<Message> send(Connection connection, List<Message> messages, Options options) {
		log.debug("OpenRouter send: model={}, messages={}",
				connection.getConfiguration() != null ? connection.getConfiguration().getModel() : null,
				messages != null ? messages.size() : 0);

		OpenRouterChatRequest request = OpenRouterChatRequest.builder()
				.model(connection.getConfiguration().getModel())
				.messages(messages.stream()
						.map(m -> OpenRouterMessage.builder()
								.role(m.getRole().name().toLowerCase())
								.content(m.getContent())
								.build())
						.collect(Collectors.toList()))
				.temperature(connection.getBehavior() != null ? connection.getBehavior().getTemperature() : null)
				.max_tokens(connection.getBehavior() != null && connection.getBehavior().getLimitations() != null ? connection.getBehavior().getLimitations().getMaxTokens() : null)
				.stream(options != null && options.isStream())
				.build();

		if (options != null && options.isStream())
			return openRouterWebClient.post()
					.uri("/chat/completions")
					.body(BodyInserters.fromValue(request))
					.accept(MediaType.TEXT_EVENT_STREAM)
					.retrieve()
					.bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
					})
					.onErrorResume(WebClientResponseException.class, e -> {
						String body = e.getResponseBodyAsString();
						log.warn("OpenRouter error: {} - {}", e.getStatusCode(), body);
						return Flux.error(new ProviderRequestException("OpenRouter request failed: " + e.getStatusCode() + " - " + body, e));
					})
					.mapNotNull(ServerSentEvent::data)
					.filter(data -> data != null && !data.isBlank())
					.flatMapIterable(this::parseSseData)
					.takeUntil(msg -> msg.getMetadata() != null && Boolean.TRUE.equals(msg.getMetadata().getOrDefault("done", false)));

		return openRouterWebClient.post()
				.uri("/chat/completions")
				.body(BodyInserters.fromValue(request))
				.retrieve()
				.bodyToMono(OpenRouterChatResponse.class)
				.onErrorResume(WebClientResponseException.class, e -> {
					String body = e.getResponseBodyAsString();
					log.warn("OpenRouter error: {} - {}", e.getStatusCode(), body);
					return Mono.error(new ProviderRequestException("OpenRouter request failed: " + e.getStatusCode() + " - " + body, e));
				})
				.flatMapMany(resp -> {
					log.trace("OpenRouter response: choices={}", resp.getChoices() != null ? resp.getChoices().size() : 0);
					if (resp.getChoices() == null || resp.getChoices().isEmpty()) {
						return Flux.empty();
					}
					String content = resp.getChoices().getFirst().getMessage().getContent();
					return Flux.just(Message.builder()
							.role(Message.Role.ASSISTANT)
							.content(content)
							.build());
				});
	}

	private List<Message> parseSseData(String data) {
		if ("[DONE]".equals(data)) {
			Message done = Message.builder()
					.role(Message.Role.ASSISTANT)
					.content("")
					.metadata(Map.of("done", true))
					.build();

			return List.of(done);
		}

		try {
			JsonNode json = MAPPER.readTree(data);
			JsonNode choices = json.get("choices");
			if (choices != null && choices.isArray() && !choices.isEmpty()) {
				JsonNode delta = choices.get(0).get("delta");
				String content = delta != null && delta.has("content") && !delta.get("content").isNull()
						? delta.get("content").asText() : null;
				if (content != null && !content.isEmpty()) {
					Message m = Message.builder()
							.role(Message.Role.ASSISTANT)
							.content(content)
							.build();
					return List.of(m);
				}
			}
		} catch (Exception ex) {
			log.debug("Failed to parse SSE data chunk: {}", data, ex);
		}

		return List.of();
	}
}