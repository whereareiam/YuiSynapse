package me.whereareiam.yuisynapse.common.provider;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.api.exception.ProviderNotFoundException;
import me.whereareiam.yuisynapse.api.output.ProviderClient;
import me.whereareiam.yuisynapse.api.type.ProviderType;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProviderRegistry {
	private final Map<ProviderType, ProviderClient> clients = new EnumMap<>(ProviderType.class);

	public ProviderRegistry(List<ProviderClient> clients) {
		for (ProviderClient client : clients) {
			this.clients.put(client.type(), client);
			log.info("Registered provider client: {}", client.type());
		}
	}

	public ProviderClient get(ProviderType type) {
		ProviderClient client = clients.get(type);
		if (client == null) throw new ProviderNotFoundException(type);
		return client;
	}
}
