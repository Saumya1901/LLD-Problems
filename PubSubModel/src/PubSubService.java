public interface PubSubService {
    void subscribe(String topic, Subscriber subscriber);

    void unsubscribe(String topic, Subscriber subscriber);

    void publish(String topic, String message);
}
