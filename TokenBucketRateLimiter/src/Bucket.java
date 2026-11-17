public class Bucket {
    private int currentTokens;
    private final int refillRate;
    private final int capacity;
    private long lastRefillTime;

    public Bucket(int capacity, int refillRate) {
        this.refillRate = refillRate;
        this.capacity = capacity;
        this.currentTokens = capacity;
        this.lastRefillTime = System.currentTimeMillis()/1000;
    }


    public synchronized boolean canConsume(){
        long currentTime  = System.currentTimeMillis()/1000;
        int timePassed = Math.toIntExact(currentTime - lastRefillTime);
        currentTokens = Math.min(capacity,currentTokens+timePassed*refillRate);
        lastRefillTime = currentTime;
        if(currentTokens>=1){
            currentTokens--;
            return true;
        }
        return false;
    }
}
