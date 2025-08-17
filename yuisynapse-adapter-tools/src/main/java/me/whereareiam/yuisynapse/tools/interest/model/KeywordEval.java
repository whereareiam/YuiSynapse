package me.whereareiam.yuisynapse.tools.interest.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class KeywordEval {
	private double score;
	private boolean anyMatched;
	private int total;
}