package me.whereareiam.yuisynapse.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
	public enum Role {
		SYSTEM, USER, ASSISTANT, TOOL
	}

	private String id;
	private Role role;
	private String content;
	private Instant timestamp;
	private Map<String, Object> metadata;

	private UserInfo author;

	@Getter
	@Setter
	@SuperBuilder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserInfo {
		private String id;
		private String username;
		private String nickname;
		private String preferredLanguage;
		private String displayName;
	}
}