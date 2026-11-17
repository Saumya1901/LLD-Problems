package models;

public class Response {
    private boolean isAllowed;
    private int retryAfter;
    private int quotaLeft;

    public Response(boolean isAllowed, int retryAfter, int quotaLeft) {
        this.isAllowed = isAllowed;
        this.retryAfter = retryAfter;
        this.quotaLeft = quotaLeft;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public int getRetryAfter() {
        return retryAfter;
    }

    public int getQuotaLeft() {
        return quotaLeft;
    }

    public void setAllowed(boolean allowed) {
        isAllowed = allowed;
    }

    public void setRetryAfter(int retryAfter) {
        this.retryAfter = retryAfter;
    }

    public void setQuotaLeft(int quotaLeft) {
        this.quotaLeft = quotaLeft;
    }
}
