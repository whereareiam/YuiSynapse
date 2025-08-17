package me.whereareiam.yuisynapse.api.model.tool;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple context map to pass metadata between tools in a pipeline execution.
 */
public final class ToolRunContext {
	private final Map<String, Object> data = new HashMap<>();

	private ToolRunContext() {
	}

	public static ToolRunContext create() {
		return new ToolRunContext();
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T) data.get(key);
	}

	public void put(String key, Object value) {
		data.put(key, value);
	}
}


