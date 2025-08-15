package me.whereareiam.yuisynapse.common.connection;

import me.whereareiam.yuisynapse.api.model.Connection;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
	private final Map<String, Connection> connections = new ConcurrentHashMap<>();

	public Connection create(Connection connection) {
		if (connection.getId() == null || connection.getId().isBlank())
			connection.setId(UUID.randomUUID().toString());

		connections.put(connection.getId(), connection);
		return connection;
	}

	public Optional<Connection> get(String id) {
		return Optional.ofNullable(connections.get(id));
	}

	public void close(String id) {
		connections.remove(id);
	}
}


