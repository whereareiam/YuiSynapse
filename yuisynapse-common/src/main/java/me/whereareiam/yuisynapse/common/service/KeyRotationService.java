package me.whereareiam.yuisynapse.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yuisynapse.api.model.Provider;
import me.whereareiam.yuisynapse.api.model.config.SynapseSettings;
import me.whereareiam.yuisynapse.api.output.KeyProvider;
import me.whereareiam.yuisynapse.api.type.ProviderType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeyRotationService implements KeyProvider {
	private final SynapseSettings settings;

	/**
	 * Per provider type, per key cooldown until time.
	 */
	private final Map<ProviderType, Map<String, Instant>> cooldownUntil = new ConcurrentHashMap<>();

	private static final Duration DEFAULT_COOLDOWN = Duration.ofSeconds(30);

	@Override
	public synchronized String acquire(ProviderType providerType) {
		Provider provider = settings.getProviders().get(providerType);
		if (provider == null || provider.getKeys() == null || provider.getKeys().isEmpty()) {
			log.warn("No keys configured for provider={}", providerType);
			return null;
		}

		Instant now = Instant.now();
		cooldownUntil.computeIfAbsent(providerType, k -> new ConcurrentHashMap<>());

		List<String> keys = provider.getKeys();
		for (String key : keys) {
			Instant until = cooldownUntil.get(providerType).get(key);
			if (until != null && until.isAfter(now))
				continue;

			return key;
		}

		return null;
	}

	@Override
	public synchronized void onSuccess(ProviderType providerType, String key) {
		// nothing additional for now
	}

	@Override
	public synchronized void onRateLimited(ProviderType providerType, String key) {
		cooldown(providerType, key, resolveCooldown(providerType));
	}

	@Override
	public synchronized void onFailure(ProviderType providerType, String key) {
		cooldown(providerType, key, resolveCooldown(providerType));
	}

	@Override
	public synchronized int keyCapacity(ProviderType providerType) {
		Provider provider = settings.getProviders().get(providerType);
		if (provider == null || provider.getKeys() == null) return 0;
		return provider.getKeys().size();
	}

	private void cooldown(ProviderType providerType, String key, Duration duration) {
		cooldownUntil.computeIfAbsent(providerType, k -> new HashMap<>())
				.put(key, Instant.now().plus(duration));
	}

	private Duration resolveCooldown(ProviderType providerType) {
		Provider provider = settings.getProviders().get(providerType);
		if (provider != null && provider.getRateLimitCooldown() != null && provider.getRateLimitCooldown() > 0) {
			return Duration.ofSeconds(provider.getRateLimitCooldown());
		}
		return DEFAULT_COOLDOWN;
	}
}


