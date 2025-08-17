package me.whereareiam.yuisynapse.api.input;

import me.whereareiam.yuisynapse.api.model.Message;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface HistoryStore {
	void append(String userKey, String channelKey, List<Message> messages);

	List<Message> read(String userKey, String channelKey, Integer retentionCount, Duration ttl);

	Instant lastAssistantAt(String userKey, String channelKey);
}