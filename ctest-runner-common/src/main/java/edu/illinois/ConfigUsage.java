package edu.illinois;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Author: Shuai Wang
 * Date:  11/8/23
 */
// TODO: make two field optional
public class ConfigUsage {
    /** Class-level configuration parameters that are used by all test methods in the class */
    private Set<String> classLevelParams = new HashSet<>();
    /** Map from method name to set of parameters */
    private Map<String, Set<String>> methodLevelParams = new HashMap<>();

    /**
     * Add a set of class-level parameters.
     */
    public void addClassLevelParams(Set<String> classLevelParams) {
        this.classLevelParams.addAll(classLevelParams);
    }

    /**
     * Add a set of method-level parameters.
     * @param fullTestMethodName the full name of the test method,
     *                           including the class name with the format of "class_method"
     * @param methodLevelParams the set of method-level parameters
     */
    public void addMethodLevelParams(String fullTestMethodName, Set<String> methodLevelParams) {
        Set<String> params = new HashSet<>(methodLevelParams);
        this.methodLevelParams.put(fullTestMethodName, Collections.unmodifiableSet(params));
    }

    /**
     * Get the set of class-level parameters.
     */
    public Set<String> getClassLevelParams() {
        return classLevelParams;
    }

    /**
     * Get the map from method name to set of parameters.
     */
    public Map<String, Set<String>> getMethodLevelParams() {
        return methodLevelParams;
    }


    public static ConfigUsage fromFile(String path) throws IOException {
        Gson gson = new Gson();
        Type configUsageType = new TypeToken<ConfigUsage>(){}.getType();
        return gson.fromJson(Utils.readStringFromFile(path), configUsageType);
    }

    /**
     * Convert the json string to ConfigUsage object.
     */
    public static ConfigUsage fromJson(String json) {
        Gson gson = new Gson();
        Type configUsageType = new TypeToken<ConfigUsage>(){}.getType();
        return gson.fromJson(json, configUsageType);
    }

    /**
     * Write the ConfigUsage to a JSON file.
     */
    public static void writeToJson(ConfigUsage config, File path)  {
        Gson gson = new Gson();
        String json = gson.toJson(config);
        Utils.writeStringToFile(path.getAbsolutePath(), json);
    }
}
