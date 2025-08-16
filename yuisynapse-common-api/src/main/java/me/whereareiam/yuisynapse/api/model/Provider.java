package me.whereareiam.yuisynapse.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
public class Provider {
	private boolean enabled;
	private String url;
	/**
	 * List of API keys that can be rotated/selected based on availability and rate limits.
	 */
	private List<String> keys;
	/**
	 * Optional cooldown to apply (in seconds) when a key is rate-limited (HTTP 429)
	 * or temporarily failing. Default is 30 seconds if not provided.
	 */
	private Integer rateLimitCooldown;
}
