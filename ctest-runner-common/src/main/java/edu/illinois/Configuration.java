package edu.illinois;

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
        ConfigTracker.injectConfig(this::set);
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
    public String get(String name, String defaultValue) {
        String value = get(name);
        if (value.equals("null")) {
            return defaultValue;
        }
        return value;
    }

    public void set(String name, String value) {
        ConfigTracker.markParmaAsSet(name);
        configMap.put(name, value);
    }
}
