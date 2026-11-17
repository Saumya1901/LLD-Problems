
public class Main {
    public static void main(String[] args) {

        PubSubService service = new PubSubServiceImpl();

        Subscriber s1 = new PrintSubscriber("S1");
        Subscriber s2 = new PrintSubscriber("S2");

        // Test 1: Subscribe two subscribers
        service.subscribe("sports", s1);
        service.subscribe("sports", s2);

        // Test 2: Publish to both
        System.out.println("--- First Publish ---");
        service.publish("sports", "Match started");

        // Test 3: Unsubscribe S1
        service.unsubscribe("sports", s1);

        // Test 4: Only S2 should receive
        System.out.println("--- After Unsubscribe ---");
        service.publish("sports", "Match ended");

        // Test 5: Duplicate subscription
        service.subscribe("sports", s2);
        System.out.println("--- Duplicate Subscription ---");
        service.publish("sports", "Final score announced");

        // Test 6: Unknown topic
        System.out.println("--- Unknown Topic ---");
        service.publish("news", "Breaking news");

        System.out.println("All tests executed!");
    }
}
