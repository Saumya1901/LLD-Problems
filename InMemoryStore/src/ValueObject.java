import java.util.concurrent.atomic.AtomicBoolean;

public final class ValueObject {
    private int ttl;
    private String value;
    private long createdAt;

    public ValueObject(int ttl, String value) {
        this.ttl = ttl;
        this.value = value;
        this.createdAt = System.currentTimeMillis();
    }


    public String getValue() {
        return value;
    }

    public int getTtl() {
        return ttl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public synchronized boolean isKeyExpired() {
//contract ttl >=0
        return ttl != -1 && (System.currentTimeMillis() - createdAt) >= ttl;
    }

 /*Immutatble */

}
