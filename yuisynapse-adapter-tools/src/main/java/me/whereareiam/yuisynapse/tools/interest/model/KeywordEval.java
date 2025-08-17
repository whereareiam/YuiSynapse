package me.whereareiam.yuisynapse.tools.interest.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class KeywordEval {
	// Ratio of positive matches over positive total [0..1]
	private double positiveScore;
	// Ratio of negative matches over negative total [0..1]
	private double negativeScore;
	// Signed score in [-1..1] computed as positiveScore - negativeScore
	private double signedScore;
	private boolean anyPositiveMatched;
	private boolean anyNegativeMatched;
	private int positiveTotal;
	private int negativeTotal;
}