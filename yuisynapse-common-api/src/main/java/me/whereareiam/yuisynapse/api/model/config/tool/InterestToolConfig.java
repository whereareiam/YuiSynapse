package me.whereareiam.yuisynapse.api.model.config.tool;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolConfig;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class InterestToolConfig extends ToolConfig {
	private double threshold;
	private boolean passIfMentioned;
	private boolean passIfKeyword;
	private boolean passIfContinuation;
	private double continuationAllowMin;

	private double mentionWeight;
	private double keywordWeight;
	private double continuationWeight;
	private int continuationHalfLifeSeconds;

	private List<String> mentions;
	private List<String> prefixes;
	/**
	 * Positive keywords raise interest. Keys are locales (e.g., "*", "en", "en-US").
	 */
	private Map<String, List<String>> positiveKeywords;
	/**
	 * Negative keywords decrease interest. Keys are locales (e.g., "*", "en", "en-US").
	 */
	private Map<String, List<String>> negativeKeywords;
}


