package models;

public enum RateLimiterType {
    TOKEN_BUCKET,
    LEAKY,
    SLIDING_LOG,
    SLIDING_WINDOW,
    FIXED_WINDOW;
}
