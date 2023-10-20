import edu.illinois.ConfigTracker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A fake configuration class that used for testing.
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class Configuration {
    Map<String, String> configMap = new HashMap<>();
    public Configuration()  {
        try{
            ConfigTracker.injectConfig(this::set);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String get(String name) {
        ConfigTracker.markParamAsUsed(name);
        if (configMap.containsKey(name)) {
            System.out.println("In get: " + name + " value: " + configMap.get(name));
            return configMap.get(name);
        }
        System.out.println("In get: " + name + " value: null");
        return "null";
    }
    public void set(String name, String value) {
        configMap.put(name, value);
    }
}
