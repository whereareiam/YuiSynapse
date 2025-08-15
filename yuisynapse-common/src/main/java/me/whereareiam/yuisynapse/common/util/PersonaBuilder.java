package me.whereareiam.yuisynapse.common.util;

import me.whereareiam.yuisynapse.api.model.Connection;

public final class PersonaBuilder {
	private PersonaBuilder() {
	}

	public static String buildPersonaPrompt(Connection.Behavior behavior) {
		if (behavior == null || behavior.getPersona() == null || behavior.getPersona().isEmpty())
			return "";

		StringBuilder builder = new StringBuilder();
		behavior.getPersona().forEach((category, lines) -> {
			builder.append(category).append("\n");
			if (lines != null && !lines.isEmpty()) {
				builder.append(String.join("\n", lines)).append("\n\n");
			} else {
				builder.append("\n");
			}
		});

		return builder.toString().trim();
	}
}


