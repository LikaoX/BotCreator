package bot;

import java.util.HashMap;
import java.util.StringJoiner;

public class DataSet {
    private HashMap<String, Object> hashMap = new HashMap<>();

    public Object get(Object key) {
        return hashMap.get(key);
    }

    public Object put(String key, Object value) {
        return hashMap.put(key, value);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        for(String key : hashMap.keySet())
            joiner.add(key + "=" + hashMap.get(key).toString());
        return "[" + joiner.toString() + "]";
    }
}
