import edu.illinois.ConfigTracker;

import java.util.HashMap;
import java.util.Map;

/**
 * A fake configuration class that used for testing.
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class Configuration {
    Map<String, String> configMap = new HashMap<>();
    public Configuration() {
        String paramMap = System.getProperty("configInjection");
        // TODO: Support dynamic bytecode instrumentation
        if (paramMap != null) {
            String[] params = paramMap.split(",");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    set(pair[0], pair[1]);
                }
            }
        }
    }
    public String get(String name) {
        ConfigTracker.markParamAsUsed(name);
        if (configMap.containsKey(name)) {
            return configMap.get(name);
        }
        return "null";
    }
    public void set(String name, String value) {
        configMap.put(name, value);
    }
}
