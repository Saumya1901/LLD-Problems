package factory;

import models.RateLimiterType;
import strategy.IRateLimiterAlgorithm;
import strategy.TokenBucketAlgorithm;

public class RateLimiterFactory {


    public IRateLimiterAlgorithm createAlgorithm(int refillRate, int maxCapacity, RateLimiterType rateLimiterType){
        switch (rateLimiterType){
            case RateLimiterType.TOKEN_BUCKET : return createTokenBucketAlgorithm(refillRate,maxCapacity);
            default: return createTokenBucketAlgorithm(refillRate,maxCapacity);
        }
    }

    private IRateLimiterAlgorithm createTokenBucketAlgorithm(int refillRate , int maxCapacity){
        return new TokenBucketAlgorithm(refillRate,maxCapacity);
    }
}
