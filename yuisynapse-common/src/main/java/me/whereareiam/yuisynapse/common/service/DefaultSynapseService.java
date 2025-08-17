package me.whereareiam.yuisynapse.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.api.MetaKeys;
import me.whereareiam.yuisynapse.api.exception.ConnectionNotFoundException;
import me.whereareiam.yuisynapse.api.input.HistoryStore;
import me.whereareiam.yuisynapse.api.input.SynapseService;
import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.Options;
import me.whereareiam.yuisynapse.api.output.ProviderClient;
import me.whereareiam.yuisynapse.api.tool.pipeline.ToolPipeline;
import me.whereareiam.yuisynapse.api.tool.pipeline.ToolPipelineResult;
import me.whereareiam.yuisynapse.common.connection.ConnectionManager;
import me.whereareiam.yuisynapse.common.provider.ProviderRegistry;
import me.whereareiam.yuisynapse.common.util.PersonaBuilder;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSynapseService implements SynapseService {
	private final ProviderRegistry providerRegistry;
	private final ConnectionManager connectionManager;
	private final ToolPipeline toolPipeline;
	private final HistoryStore historyStore;

	@Override
	public CompletionStage<Connection> create(Connection connection) {
		log.debug("Creating connection: provider={}, model={}",
				connection.getConfiguration() != null ? connection.getConfiguration().getProvider() : null,
				connection.getConfiguration() != null ? connection.getConfiguration().getModel() : null);

		return CompletableFuture.completedStage(connectionManager.create(connection));
	}

	@Override
	public Publisher<Message> send(Connection connection, Message message) {
		return send(connection, message, Options.builder().stream(false).build());
	}

	@Override
	public Publisher<Message> send(Connection connection, List<Message> messages) {
		return send(connection, messages, Options.builder().stream(false).build());
	}

	@Override
	public Publisher<Message> send(Connection connection, Message message, Options options) {
		return send(connection, List.of(enrich(message)), options);
	}

	@Override
	public Publisher<Message> send(Connection connection, List<Message> messages, Options options) {
		if (messages.isEmpty())
			return Flux.empty();

		Connection persisted = connectionManager.get(connection.getId()).orElseThrow(() -> new ConnectionNotFoundException(connection.getId()));
		log.debug("Sending messages: connectionId={}, count={}", persisted.getId(), messages.size());

		List<Message> initial = prependSystemPersonaIfPresent(persisted, messages);

		ToolPipelineResult pipelineResult = toolPipeline.run(persisted, initial);
		if (!pipelineResult.isShouldForward()) {
			log.debug("Interest gate blocked forwarding: connectionId={}", persisted.getId());
			return Flux.empty();
		}

		ProviderClient client = providerRegistry.get(persisted.getConfiguration().getProvider());
		return Flux.from(client.send(persisted, pipelineResult.getMessages(), options))
				.doOnNext(this::timestampIfMissing)
				.doOnNext(m -> {
					if (m.getRole() == Message.Role.ASSISTANT) {
						try {
							String userKey = messages.stream().filter(x -> x.getRole() == Message.Role.USER && x.getAuthor() != null && x.getAuthor().getId() != null)
									.map(x -> x.getAuthor().getId()).findFirst().orElse(null);
							String channelKey = messages.stream().filter(x -> x.getMetadata() != null && x.getMetadata().get(MetaKeys.CHANNEL_ID) != null)
									.map(x -> String.valueOf(x.getMetadata().get(MetaKeys.CHANNEL_ID))).findFirst().orElse(null);
							historyStore.append(userKey, channelKey, List.of(m));
						} catch (Exception ignored) {
						}
					}
				})
				.doOnError(err -> log.error("Provider send failed: connectionId={}, err={}", persisted.getId(), err.toString()));
	}

	@Override
	public CompletionStage<Void> close(Connection connection) {
		log.debug("Closing connection: connectionId={}", connection.getId());
		connectionManager.close(connection.getId());

		return CompletableFuture.completedStage(null);
	}

	private Message enrich(Message message) {
		timestampIfMissing(message);

		Map<String, Object> metadata = message.getMetadata();
		if (metadata == null) {
			metadata = new HashMap<>();
			message.setMetadata(metadata);
		}

		return message;
	}

	private List<Message> prependSystemPersonaIfPresent(Connection connection, List<Message> messages) {
		String persona = PersonaBuilder.buildPersonaPrompt(connection.getBehavior());
		if (persona.isBlank())
			return messages;

		Message system = Message.builder()
				.role(Message.Role.SYSTEM)
				.content(persona)
				.timestamp(Instant.now())
				.metadata(Map.of())
				.build();

		List<Message> result = new ArrayList<>(messages.size() + 1);
		result.add(system);
		result.addAll(messages);
		return result;
	}

	private void timestampIfMissing(Message m) {
		if (m.getTimestamp() == null) {
			m.setTimestamp(Instant.now());
		}
	}
}


