package me.whereareiam.yuisynapse.adapter.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import me.whereareiam.yuisynapse.api.model.config.tool.HistoryToolConfig;
import me.whereareiam.yuisynapse.api.model.config.tool.InterestToolConfig;
import me.whereareiam.yuisynapse.api.model.config.tool.LanguageEnforcerToolConfig;
import me.whereareiam.yuisynapse.api.model.config.tool.UserInfoToolConfig;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolConfig;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolDefinition;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Polymorphic deserializer for ToolConfig.
 * - If deserializing a map entry: use the map key.
 * - If deserializing ToolDefinition.config: use the parent ToolDefinition.name.
 * - Otherwise, allow inline discriminators in the config object: "type" | "tool" | "name".
 */
public class ToolConfigDeserializer extends JsonDeserializer<ToolConfig> {
	private static final Map<String, Class<? extends ToolConfig>> REGISTRY;

	static {
		Map<String, Class<? extends ToolConfig>> m = new LinkedHashMap<>();
		m.put("history", HistoryToolConfig.class);
		m.put("userinfo", UserInfoToolConfig.class);
		m.put("interest", InterestToolConfig.class);
		m.put("languageenforcer", LanguageEnforcerToolConfig.class);
		REGISTRY = Collections.unmodifiableMap(m);
	}

	@Override
	public ToolConfig deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		ObjectCodec codec = p.getCodec();
		JsonNode node = codec.readTree(p);

		String rawKey = detectTypeKey(node, p);
		if (rawKey == null || rawKey.isBlank()) {
			throw JsonMappingException.from(p,
					"Unable to determine tool type for ToolConfig. " +
							"Provide one of: {\"type\"|\"tool\"|\"name\"} inside the config, " +
							"or ensure it's nested under a ToolDefinition with a 'name'.");
		}

		String normalized = normalize(rawKey);
		Class<? extends ToolConfig> target = REGISTRY.get(normalized);

		if (target == null) {
			throw JsonMappingException.from(p,
					"Unknown tool key: '" + rawKey + "'. Supported: history, userInfo, interest, languageEnforcer");
		}
		return codec.treeToValue(node, target);
	}

	private static String detectTypeKey(JsonNode node, JsonParser p) {
		// 1) Inline discriminators inside the config object itself
		if (node.has("type")) return node.get("type").asText();
		if (node.has("tool")) return node.get("tool").asText();
		if (node.has("name")) return node.get("name").asText();

		// 2) If we are deserializing a map-like structure, the current field name may be the key
		JsonStreamContext ctx = p.getParsingContext();
		String fieldName = ctx != null ? ctx.getCurrentName() : null;

		// 3) Special case: when deserializing ToolDefinition.config, the field name is "config"
		//    → look up to the parent bean (ToolDefinition) and use its 'name' as the discriminator.
		if ("config".equals(fieldName)) {
			for (JsonStreamContext c = ctx; c != null; c = c.getParent()) {
				Object currentValue = c.getCurrentValue();
				if (currentValue instanceof ToolDefinition td) {
					if (td.getName() != null && !td.getName().isBlank()) {
						return td.getName();
					}
				}
			}
			// If parent ToolDefinition.name is not available yet, keep going to other fallbacks.
		}

		// 4) Fallbacks for keyed structures where fieldName *is* the discriminator
		if (fieldName != null && !fieldName.isBlank()) return fieldName;
		if (ctx != null && ctx.getParent() != null && ctx.getParent().getCurrentName() != null) {
			return ctx.getParent().getCurrentName();
		}
		return null;
	}

	private static String normalize(String s) {
		return s.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}
}
