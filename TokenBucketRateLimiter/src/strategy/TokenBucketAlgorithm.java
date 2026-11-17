package strategy;

public class TokenBucketAlgorithm implements IRateLimiterAlgorithm{
    private final int refillRate;
    private final int maxCapacity;
    public TokenBucketAlgorithm(int refillRate , int maxCapacity) {
        this.refillRate = refillRate;
        this.maxCapacity = maxCapacity;
    }

    @Override
    public boolean allowRequest(String clientId) {
        return false;
    }
}
