package strategy;

public interface IRateLimiterAlgorithm {
     boolean allowRequest(String clientId);
}
