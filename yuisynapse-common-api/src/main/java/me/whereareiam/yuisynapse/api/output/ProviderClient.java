package me.whereareiam.yuisynapse.api.output;

import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.Options;
import me.whereareiam.yuisynapse.api.type.ProviderType;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Provider-specific transport client. Implementations live in provider modules.
 */
public interface ProviderClient {
	ProviderType type();

	default Publisher<Message> send(Connection connection, List<Message> messages) {
		return send(connection, messages, Options.builder().stream(false).build());
	}

	Publisher<Message> send(Connection connection, List<Message> messages, Options options);

	default CompletionStage<Void> close(Connection connection) {
		return CompletableFuture.completedStage(null);
	}
}


