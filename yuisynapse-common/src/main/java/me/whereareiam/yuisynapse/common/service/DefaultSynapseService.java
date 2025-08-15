package me.whereareiam.yuisynapse.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.api.exception.ConnectionNotFoundException;
import me.whereareiam.yuisynapse.api.input.SynapseService;
import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.Options;
import me.whereareiam.yuisynapse.api.output.ProviderClient;
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

	private final ConnectionManager connectionManager = new ConnectionManager();

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
		Connection persisted = connectionManager.get(connection.getId()).orElseThrow(() -> new ConnectionNotFoundException(connection.getId()));
		log.debug("Sending messages: connectionId={}, count={}", persisted.getId(), messages != null ? messages.size() : 0);

		List<Message> pipelineInput = prependSystemPersonaIfPresent(persisted, messages);

		ProviderClient client = providerRegistry.get(persisted.getConfiguration().getProvider());
		return Flux.from(client.send(persisted, pipelineInput, options))
				.doOnNext(this::timestampIfMissing)
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


