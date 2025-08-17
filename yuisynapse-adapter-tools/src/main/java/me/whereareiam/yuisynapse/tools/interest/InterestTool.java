package me.whereareiam.yuisynapse.tools.interest;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yuisynapse.api.MetaKeys;
import me.whereareiam.yuisynapse.api.input.HistoryStore;
import me.whereareiam.yuisynapse.api.model.Connection;
import me.whereareiam.yuisynapse.api.model.Message;
import me.whereareiam.yuisynapse.api.model.config.tool.InterestToolConfig;
import me.whereareiam.yuisynapse.api.model.config.tool.base.ToolConfig;
import me.whereareiam.yuisynapse.api.model.tool.ToolResult;
import me.whereareiam.yuisynapse.api.model.tool.ToolRunContext;
import me.whereareiam.yuisynapse.api.tool.Tool;
import me.whereareiam.yuisynapse.api.type.ToolPhase;
import me.whereareiam.yuisynapse.tools.ToolMetadataKeys;
import me.whereareiam.yuisynapse.tools.interest.model.KeywordEval;
import me.whereareiam.yuisynapse.tools.interest.model.KeywordMatcher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class InterestTool implements Tool {
	private final HistoryStore historyStore;
	private InterestToolConfig toolConfig;

	@Override
	public String name() {
		return "INTEREST";
	}

	@Override
	public ToolPhase phase() {
		return ToolPhase.PRE_PROCESSING;
	}

	@Override
	public void configure(ToolConfig config) {
		if (!(config instanceof InterestToolConfig c)) return;

		this.toolConfig = c;
	}

	@Override
	public ToolResult execute(Connection connection, List<Message> input, ToolRunContext runContext) {
		if (input == null || input.isEmpty()) {
			return ToolResult.builder()
					.shouldForward(false)
					.build();
		}

		Message user = input.stream()
				.filter(m -> m.getRole() == Message.Role.USER)
				.findFirst()
				.orElse(null);

		if (user == null || safe(user.getContent()).isBlank()) {
			return ToolResult.builder()
					.shouldForward(false)
					.build();
		}

		final String content = safe(user.getContent()).stripLeading();
		final String contentLower = content.toLowerCase();

		boolean mentioned = containsMention(content) || isReplyToBot(user);
		boolean prefixed = hasPrefix(contentLower);

		KeywordEval kw = computeKeywordScore(user, contentLower);

		double continuation = computeContinuationBoost(user);
		double rawScore =
				toolConfig.getMentionWeight() * ((mentioned || prefixed) ? 1.0 : 0.0) +
					toolConfig.getKeywordWeight() * kw.getSignedScore() +
					toolConfig.getContinuationWeight() * continuation;
		double score = clamp01(rawScore);

		runContext.put(ToolMetadataKeys.INTEREST_PROBABILITY, score);

		boolean allow =
				(toolConfig.isPassIfMentioned() && (mentioned || prefixed)) ||
					(toolConfig.isPassIfKeyword() && kw.isAnyPositiveMatched()) ||
					(toolConfig.isPassIfContinuation() && continuation >= toolConfig.getContinuationAllowMin()) ||
					score >= toolConfig.getThreshold();

		Map<String, Object> metadata = new HashMap<>();
		metadata.put(ToolMetadataKeys.INTEREST_PROBABILITY, score);
		metadata.put(ToolMetadataKeys.THRESHOLD, toolConfig.getThreshold());

		return ToolResult.builder()
				.shouldForward(allow)
				.interestScore(score)
				.metadata(metadata)
				.build();
	}

	private boolean containsMention(String content) {
		List<String> mentions = toolConfig != null ? toolConfig.getMentions() : null;
		if (content == null || mentions == null || mentions.isEmpty())
			return false;

		for (String mention : mentions) {
			if (mention == null) continue;
			String m = mention.trim();
			if (m.isEmpty()) continue;

			int fromIndex = 0;
			while (true) {
				int idx = content.indexOf(m, fromIndex);
				if (idx < 0) break;

				int beforeIdx = idx - 1;
				int afterIdx = idx + m.length();

				boolean beforeOk = beforeIdx < 0 || isMentionWordChar(content.charAt(beforeIdx));
				boolean afterOk = afterIdx >= content.length() || isMentionWordChar(content.charAt(afterIdx));

				if (beforeOk && afterOk)
					return true;

				fromIndex = idx + 1;
			}
		}

		return false;
	}

	private boolean isMentionWordChar(char c) {
		return (c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9') && c != '_' && c != '-';
	}

	private boolean hasPrefix(String contentLower) {
		List<String> prefixes = toolConfig != null ? toolConfig.getPrefixes() : null;
		if (contentLower == null || prefixes == null || prefixes.isEmpty())
			return false;

		for (String p : prefixes) {
			if (p == null) continue;
			if (contentLower.startsWith(p.toLowerCase()))
				return true;
		}

		return false;
	}

	private KeywordEval computeKeywordScore(Message msg, String contentLower) {
		KeywordEval eval = new KeywordEval();

		if (contentLower == null || contentLower.isBlank()) return eval;

		Map<String, List<KeywordMatcher>> positiveByLocale = buildMatchersByLocale(toolConfig != null ? toolConfig.getPositiveKeywords() : null);
		Map<String, List<KeywordMatcher>> negativeByLocale = buildMatchersByLocale(toolConfig != null ? toolConfig.getNegativeKeywords() : null);

		String locale = detectLocale(msg);

		List<KeywordMatcher> positive = mergeForLocale(positiveByLocale, locale);
		List<KeywordMatcher> negative = mergeForLocale(negativeByLocale, locale);

		int posTotal = positive.size();
		int negTotal = negative.size();

		int posMatches = 0;
		for (KeywordMatcher km : positive) {
			if (km.matches(contentLower)) {
				posMatches++;
				eval.setAnyPositiveMatched(true);
			}
		}

		int negMatches = 0;
		for (KeywordMatcher km : negative) {
			if (km.matches(contentLower)) {
				negMatches++;
				eval.setAnyNegativeMatched(true);
			}
		}

		double posScore = posTotal > 0 ? (double) posMatches / (double) posTotal : 0.0;
		double negScore = negTotal > 0 ? (double) negMatches / (double) negTotal : 0.0;
		double signed = posScore - negScore;

		eval.setPositiveTotal(posTotal);
		eval.setNegativeTotal(negTotal);
		eval.setPositiveScore(posScore);
		eval.setNegativeScore(negScore);
		eval.setSignedScore(signed);

		return eval;
	}

	private Map<String, List<KeywordMatcher>> buildMatchersByLocale(Map<String, List<String>> rawSpec) {
		Map<String, List<KeywordMatcher>> result = new HashMap<>();
		if (rawSpec == null || rawSpec.isEmpty()) return result;

		for (Map.Entry<String, List<String>> entry : rawSpec.entrySet()) {
			String locale = entry.getKey();
			List<KeywordMatcher> keywordMatchers = new ArrayList<>();
			if (entry.getValue() != null) {
				for (String raw : entry.getValue()) {
					if (raw == null) continue;

					String keyword = raw.trim();
					if (keyword.isEmpty()) continue;

					if (keyword.startsWith("re:") && keyword.length() > 3) {
						String patternBody = keyword.substring(3);
						try {
							keywordMatchers.add(new KeywordMatcher(Pattern.compile(patternBody, Pattern.CASE_INSENSITIVE), null));
						} catch (Exception ignored) {
							keywordMatchers.add(new KeywordMatcher(null, keyword.toLowerCase()));
						}
						continue;
					}

					keywordMatchers.add(new KeywordMatcher(null, keyword.toLowerCase()));
				}
			}

			result.put(locale, keywordMatchers);
		}

		return result;
	}

	private List<KeywordMatcher> mergeForLocale(Map<String, List<KeywordMatcher>> byLocale, String locale) {
		List<KeywordMatcher> merged = new ArrayList<>();
		if (byLocale == null || byLocale.isEmpty()) return merged;

		addAll(merged, byLocale.get("*"));
		if (locale != null) {
			addAll(merged, byLocale.get(locale));
			int dash = locale.indexOf('-');
			if (dash > 0) addAll(merged, byLocale.get(locale.substring(0, dash)));
		}
		if (merged.isEmpty()) byLocale.values().forEach(list -> addAll(merged, list));
		return merged;
	}

	private double computeContinuationBoost(Message user) {
		if (historyStore == null || toolConfig == null || toolConfig.getContinuationHalfLifeSeconds() <= 0)
			return 0.0;

		String userKey = user.getAuthor() != null ? user.getAuthor().getId() : null;
		String channelKey = extractMetadata(user);
		Instant last = historyStore.lastAssistantAt(userKey, channelKey);
		if (last == null) return 0.0;

		long sec = Duration.between(last, Instant.now()).getSeconds();
		if (sec < 0) sec = 0;

		double halfLives = (double) sec / (double) toolConfig.getContinuationHalfLifeSeconds();
		double value = Math.pow(0.5, halfLives);

		return clamp01(value);
	}

	private static String detectLocale(Message msg) {
		String loc = extractMetaLower(msg, ToolMetadataKeys.LOCALE);
		if (loc == null) loc = extractMetaLower(msg, ToolMetadataKeys.LANGUAGE);
		if (loc == null) loc = extractMetaLower(msg, ToolMetadataKeys.LANG);

		return (loc != null && !loc.isBlank()) ? loc : null;
	}

	private static String extractMetaLower(Message message, String key) {
		if (message == null || message.getMetadata() == null) return null;
		Object v = message.getMetadata().get(key);

		return v != null ? String.valueOf(v).toLowerCase().trim() : null;
	}

	private static String extractMetadata(Message message) {
		if (message == null || message.getMetadata() == null) return null;
		Object v = message.getMetadata().get(MetaKeys.CHANNEL_ID);

		return v != null ? String.valueOf(v) : null;
	}

	private static boolean isReplyToBot(Message message) {
		if (message == null || message.getMetadata() == null) return false;
		Object v = message.getMetadata().get(MetaKeys.REPLY_TO_BOT);
		if (v == null) return false;

		try {
			return Boolean.parseBoolean(String.valueOf(v));
		} catch (Exception ignored) {
			return false;
		}
	}

	private static double clamp01(double v) {
		if (v < 0)
			return 0.0;

		return Math.min(v, 1.0);
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}

	private static void addAll(List<KeywordMatcher> dst, List<KeywordMatcher> src) {
		if (src != null && !src.isEmpty()) dst.addAll(src);
	}
}
