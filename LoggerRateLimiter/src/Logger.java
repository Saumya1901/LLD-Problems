import java.util.HashMap;
import java.util.Map;

public class Logger {

    private Map<String, Integer> rateLimitMap = new HashMap<>();

    public boolean shouldPrintMessage(int timestamp, String message) {
        if (timestamp <0) return false;//cannot have negetrive timestamp
        if (rateLimitMap.containsKey(message)) {
            int putTimeStamp = rateLimitMap.get(message);
            if (timestamp - putTimeStamp >= 10) {
                rateLimitMap.put(message, timestamp);
                return true;

            }
            return false;
        }

        rateLimitMap.put(message, timestamp);

        return true;
    }
/*TC: O(1)
* SC : O(N) MAXKEYS PUT */

}
