package me.whereareiam.yuisynapse.tools.interest.model;

import lombok.AllArgsConstructor;

import java.util.regex.Pattern;

@AllArgsConstructor
public final class KeywordMatcher {
	private final Pattern pattern;
	private final String literalLower;

	public boolean matches(String contentLower) {
		if (pattern != null) return pattern.matcher(contentLower).find();
		return contentLower.contains(literalLower);
	}
}