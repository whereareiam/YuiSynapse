package me.whereareiam.yuisynapse.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class Provider {
	private boolean enabled;
	private String url;
	private String key;
}
