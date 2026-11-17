import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StoringService {

    ConcurrentHashMap<String, ValueObject> hashMap = new ConcurrentHashMap<>();
    /*
    * put , remove
    * while you are reading from hashmap you cannot update it -> modification
    * lock on the bucket hash of the key
    * */

    public ValueObject get(String key) {
        //making this synchronized cause the setting of value ojects validity is handled here 2   or not
        // lets say there are 2 threads , we have the current time what if it pauses after that , we need to protect that ?
        // what if current time is taken and then it pauses and by the time it returns true , other thread comes and sets it as false
        // making the setting as synchronzied to handle this scenario
        Optional<ValueObject> valueObject = Optional.ofNullable(hashMap.get(key));
        if ( valueObject.isPresent() && !valueObject.get().isKeyExpired()) {
            return valueObject.get();
        }
        return null;
    }

    public void put(String key, String value) {

        hashMap.compute(key, (k,currVal)->
                new ValueObject(-1,value));
    }
    /*
    * a - alice
    * a - bob */

    public void delete(String key) {
           hashMap.remove(key);

    }

    public void putByTtl(String key, String value, int ttl) {
        if(ttl < 0){
            System.out.println("wrong input ");
            return;
        }
        hashMap.compute(key, (k, currVal) ->
                new ValueObject(ttl, value));

    }
}
/*

map => {key , value } ValueObject => value , ttl
ttl = -1; => persists indef ,
ttl > 0 ;
get ; map.get(key)  ;
put map.put(key,valueobj) ttl = -1
put withttl map.put(key, valueobj)-> ttl > 0
delete map.delete(key)
* */
