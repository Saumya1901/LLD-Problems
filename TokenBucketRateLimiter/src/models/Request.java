package models;

public class Request {
    private final String clientId;
    private final String endpoint;

    public Request(String clientId, String endpoint) {
        this.clientId = clientId;
        this.endpoint = endpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
