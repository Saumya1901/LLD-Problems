import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class PubSubServiceImpl implements PubSubService{
    private final ConcurrentHashMap<String , CopyOnWriteArraySet<Subscriber>> hashMap = new ConcurrentHashMap<>();
    @Override
    public void subscribe(String topic, Subscriber subscriber) {
        hashMap.computeIfAbsent(topic, k -> new CopyOnWriteArraySet<>()).add(subscriber);
    }

    @Override
    public void unsubscribe(String topic, Subscriber subscriber) {
        CopyOnWriteArraySet<Subscriber> subscribers = hashMap.get(topic);

        if (subscribers != null) {
            subscribers.remove(subscriber);
        }
    }
    @Override
    public void publish(String topic, String message) {
        CopyOnWriteArraySet<Subscriber> subscribers = hashMap.get(topic);

        if (subscribers == null) {
            return;
        }

        for (Subscriber subscriber : subscribers) {
            subscriber.update(message);
        }
    }
}
