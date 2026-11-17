public class Main {
    public static void main(String[] args)
            throws InterruptedException {

        StoringService store = new StoringService();

        // Test 1: Insert
        store.put("a", "Alice");
        assert store.get("a").getValue().equals("Alice");

        // Test 2: Overwrite
        store.put("a", "Bob");
        assert store.get("a").getValue().equals("Bob");

        // Test 3: Delete
        store.delete("a");
        assert store.get("a") == null;

        // Test 4: Missing key
        assert store.get("unknown") == null;

        // Test 5: TTL expiration
        store.putByTtl("a", "Alice", 1000);
        Thread.sleep(1500);
        assert store.get("a") == null;

        // Test 6: TTL reset
        store.putByTtl("a", "Alice", 2000);
        Thread.sleep(1000);
        store.putByTtl("a", "Bob", 2000);
        Thread.sleep(1500);
        assert store.get("a").getValue().equals("Bob");

        System.out.println("All 6 tests passed!");
    }
}