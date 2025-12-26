package me.whereareiam.yuisynapse.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Represents an error that occurred during AI interaction processing.
 * Contains details about the error including whether it can be retried.
 */
@Data
@SuperBuilder
public class SynapseError {
    private String message;
    private String code;
    private Throwable cause;
    private Instant timestamp;
    private String sessionId;
    private boolean retryable;
}
