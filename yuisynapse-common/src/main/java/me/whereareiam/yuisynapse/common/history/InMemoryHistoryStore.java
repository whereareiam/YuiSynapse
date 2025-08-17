package me.whereareiam.yuisynapse.common.history;

import me.whereareiam.yuisynapse.api.input.HistoryStore;
import me.whereareiam.yuisynapse.api.model.Message;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class InMemoryHistoryStore implements HistoryStore {
	private final Map<String, Deque<Message>> perUser = new ConcurrentHashMap<>();
	private final Map<String, Deque<Message>> perChannel = new ConcurrentHashMap<>();
	private final Map<String, java.time.Instant> lastAssistantByChannel = new ConcurrentHashMap<>();
	private final Map<String, java.time.Instant> lastAssistantByUser = new ConcurrentHashMap<>();

	@Override
	public void append(String userKey, String channelKey, List<Message> messages) {
		List<Message> copies = copyWithTimestamp(messages);
		if (userKey != null) {
			Deque<Message> dq = perUser.computeIfAbsent(userKey, _ -> new ArrayDeque<>());
			dq.addAll(copies);
		}
		if (channelKey != null) {
			Deque<Message> dq = perChannel.computeIfAbsent(channelKey, _ -> new ArrayDeque<>());
			dq.addAll(copies);
		}

		for (Message m : copies) {
			if (m.getRole() == Message.Role.ASSISTANT) {
				if (channelKey != null) lastAssistantByChannel.put(channelKey, m.getTimestamp());
				if (userKey != null) lastAssistantByUser.put(userKey, m.getTimestamp());
			}
		}
	}

	@Override
	public List<Message> read(String userKey, String channelKey, Integer retentionCount, Duration ttl) {
		List<Message> merged = new ArrayList<>();
		if (channelKey != null) merged.addAll(read(perChannel.get(channelKey), ttl));
		if (userKey != null) merged.addAll(read(perUser.get(userKey), ttl));

		// De-duplicate by identity (timestamp+role+content hash) and sort chronologically
		Map<String, Message> uniq = new LinkedHashMap<>();
		for (Message m : merged) {
			if (m == null) continue;
			String key = keyOf(m);
			uniq.putIfAbsent(key, m);
		}
		merged = new ArrayList<>(uniq.values());
		merged.sort(Comparator.comparing(m -> m.getTimestamp() != null ? m.getTimestamp() : Instant.EPOCH));

		if (retentionCount != null && merged.size() > retentionCount)
			merged = merged.subList(merged.size() - retentionCount, merged.size());

		return merged;
	}

	private List<Message> read(Deque<Message> deque, Duration ttl) {
		if (deque == null) return List.of();
		Instant now = Instant.now();
		return deque.stream()
				.filter(m -> ttl == null || m.getTimestamp() == null || Duration.between(m.getTimestamp(), now).compareTo(ttl) <= 0)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private List<Message> copyWithTimestamp(List<Message> messages) {
		Instant now = Instant.now();
		return messages.stream().map(m -> Message.builder()
				.id(m.getId())
				.role(m.getRole())
				.content(m.getContent())
				.timestamp(m.getTimestamp() != null ? m.getTimestamp() : now)
				.metadata(m.getMetadata())
				.author(m.getAuthor())
				.build()).collect(Collectors.toList());
	}

	private String keyOf(Message m) {
		String ts = String.valueOf(m.getTimestamp() != null ? m.getTimestamp().toEpochMilli() : 0L);
		String role = m.getRole() != null ? m.getRole().name() : "";
		String content = m.getContent() != null ? m.getContent() : "";

		return ts + "|" + role + "|" + Integer.toHexString(content.hashCode());
	}

	@Override
	public Instant lastAssistantAt(String userKey, String channelKey) {
		Instant byCh = channelKey != null ? lastAssistantByChannel.get(channelKey) : null;
		Instant byUser = userKey != null ? lastAssistantByUser.get(userKey) : null;

		if (byCh == null) return byUser;
		if (byUser == null) return byCh;

		return byCh.isAfter(byUser) ? byCh : byUser;
	}
}


