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

    public void update(ConfigUsage newConfigUsage) {
        // Update the class level parameters with the new one
        this.classLevelParams = new HashSet<>(newConfigUsage.getClassLevelParams());
        // Add or Update every method level parameter
        for (Map.Entry<String, Set<String>> entry : newConfigUsage.getMethodLevelParams().entrySet()) {
            this.methodLevelParams.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
    }

    public void addEmptyMethod(String methodName) {
        methodLevelParams.put(methodName, new HashSet<>());
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
    private static void toJsonFile(ConfigUsage config, File path)  {
        Gson gson = new Gson();
        String json = gson.toJson(config);
        Utils.writeStringToFile(path.getAbsolutePath(), json);
    }

    /**
     * Update the ConfigUsage to a JSON file.
     */
    public static void writeToJson(ConfigUsage config, File path) {
        // If the file does not exist, directly write config to the path
        if (!path.exists()) {
            ConfigUsage.toJsonFile(config, path);
        }
        // Otherwise read the file and update the config
        ConfigUsage configUsageFromFile = null;
        try {
            configUsageFromFile = ConfigUsage.fromFile(path.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Cannot read the config usage file " + path.getAbsolutePath());
        }
        configUsageFromFile.update(config);
        ConfigUsage.toJsonFile(configUsageFromFile, path);
    }


    /**
     * Push the className and methodName to configUsage and wait for final update
     * The final update method @updateAllConfigUsage()
     * is called right before write the configUsage to a JSON file
     */
    public static void bufferForUpdate(ConfigUsage configUsage, String className, String methodName) {
        String fullTestName = Utils.getFullTestName(className, methodName);
        configUsage.addEmptyMethod(fullTestName);

    }

    /**
     * Update the config usage for all test methods
     */
    public static void updateAllConfigUsage(ConfigUsage configUsage) {
        for (String methodName: configUsage.getMethodLevelParams().keySet()) {
            String className = methodName.substring(0, methodName.lastIndexOf(Names.TEST_CLASS_METHOD_SEPARATOR));
            updateConfigUsage(configUsage, className, methodName);
        }
    }

    /**
     * Update the config usage for the given test method
     */
    public static void updateConfigUsage(ConfigUsage configUsage, String className, String methodName) {
        String fullTestName = Utils.getFullTestName(className, methodName);
        configUsage.addClassLevelParams(ConfigTracker.getClassUsedParams(className));
        configUsage.addMethodLevelParams(fullTestName, ConfigTracker.getMethodUsedParams(fullTestName));
    }
}
