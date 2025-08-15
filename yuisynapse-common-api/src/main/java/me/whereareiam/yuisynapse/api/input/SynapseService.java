package me.whereareiam.yuisynapse.api.input;

import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.Options;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * High-level entrypoint for creating connections and sending messages to an AI provider.
 * Implementations route requests to a provider-specific client, stream responses, and
 * abstract transport details away from callers.
 */
public interface SynapseService {
	CompletionStage<Connection> create(Connection connection);

	default Publisher<Message> send(Connection connection, Message message) {
		return send(connection, message, Options.builder().stream(false).build());
	}

	default Publisher<Message> send(Connection connection, List<Message> messages) {
		return send(connection, messages, Options.builder().stream(false).build());
	}

	default Publisher<Message> stream(Connection connection, Message message) {
		return send(connection, message, Options.builder().stream(true).build());
	}

	default Publisher<Message> stream(Connection connection, List<Message> messages) {
		return send(connection, messages, Options.builder().stream(true).build());
	}

	Publisher<Message> send(Connection connection, Message message, Options options);

	Publisher<Message> send(Connection connection, List<Message> messages, Options options);

	CompletionStage<Void> close(Connection connection);
}