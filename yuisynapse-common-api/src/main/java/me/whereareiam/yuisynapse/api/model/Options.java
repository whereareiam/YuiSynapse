package me.whereareiam.yuisynapse.api.model;

import lombok.*;

/**
 * Options controlling how a send request should be processed.
 * Currently supports toggling streaming of provider responses.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Options {
	/**
	 * When true, provider responses should be streamed as partial chunks if supported.
	 * When false, a single final message is emitted.
	 */
	private boolean stream;
}


