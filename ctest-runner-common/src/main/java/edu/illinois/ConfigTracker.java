package edu.illinois;

import edu.illinois.parser.ConfigurationParser;
import edu.illinois.parser.JsonConfigurationParser;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static edu.illinois.Names.*;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigTracker {

    /** Whether to inject config parameters from a file */
    private static final boolean injectFromFile;
    /** The map that maintains the test usage tracker for each test class and method */
    private static final ConcurrentHashMap<String, TestTracker> testTrackerMap = new ConcurrentHashMap<>();
    /** The map from test class name to the configuration file */
    private static final Map<String, File> testClassToConfigFile = new ConcurrentHashMap<>();

    static {
        injectFromFile = constructTestClzToConfigFileMap();
    }

    /**
     * Check whether the test tracker map contains the test name.
     */
    private static boolean trackerContains(String testName) {
        return testTrackerMap.containsKey(testName);
    }

    /**
     * Add a test tracker to the test tracker map.
     */
    private static void trackerAdd(String testName, TestTracker tracker) {
        testTrackerMap.put(testName, tracker);
    }

    /**
     * Get the test tracker from the test tracker map
     * If the test tracker does not exist, create a new one and add it to the map to prevent NPE.
     */
    private static TestTracker trackerGet(String testName, boolean isMethodTracker) {
        if (!trackerContains(testName)) {
            TestTracker tracker = isMethodTracker? new TestMethodTracker() : new TestClassTracker();
            trackerAdd(testName, tracker);
        }
        return testTrackerMap.get(testName);
    }

    /**
     * Start a new test method, clear the set of used parameters
     */
    public static void startTestMethod(String className, String methodName) {
        // Stop tracking class-level parameters once a test method is started
        if (!trackerContains(className)) {
            trackerAdd(className, new TestClassTracker());
        }
        ((TestClassTracker) trackerGet(className, false)).stopTrackingClassParam();
        // Start tracking method-level parameters with a new TestMethodTracker
        trackerAdd(Utils.getFullTestName(className, methodName), new TestMethodTracker());
    }

    /**
     * Check whether a parameter has been used in the current test method or class
     * @param param the parameter to check
     * @return true if the parameter has been used, false otherwise
     */
    public static boolean isParameterUsed(String className, String methodName, String param) {
        return (trackerGet(className, false)).isParameterUsed(param) ||
                (trackerGet(Utils.getFullTestName(className, methodName), true)).isParameterUsed(param);
    }

    /**
     * Check whether a parameter has been set in the current test method or class
     * @param param the parameter to check
     * @return true if the parameter has been set, false otherwise
     */
    public static boolean isParameterSet(String className, String methodName, String param) {
        return (trackerGet(className, false)).isParameterSet(param) ||
                (trackerGet(Utils.getFullTestName(className, methodName), true)).isParameterSet(param);
    }

    /**
     * Mark a parameter as used in the current test method or class
     * @param param the parameter to mark
     */
    public static void markParamAsUsed(String param) {
        String className = Utils.inferTestClassNameFromStackTrace();
        if (((TestClassTracker) trackerGet(className, false)).isTrackingClassParam()){
            trackerGet(className, false).addUsedParam(param);
        } else {
            try {
                trackerGet(Utils.getTestClassAndMethodName()[1], true).addUsedParam(param);
            } catch (IOException e) {
                // If catch IOException, it means that we are still in class-level setup method
                trackerGet(className, false).addUsedParam(param);
            }
        }
    }

    /**
     * Not only mark a parameter as used, but also inject the parameter into the configuration class.
     * The purpose of this method is to make the configuration API instrumentation easier.
     */
    public static <T> void markParamAsUsed(int confObjectId, BiConsumer<String, T> configSetterMethod, String param) {
        injectConfig(confObjectId, configSetterMethod);
        markParamAsUsed(param);
    }

    /**
     * Mark a parameter as set in the current test method or class
     * @param param the parameter that has been set
     */
    public static void markParamAsSet(String param) {
        String className = Utils.inferTestClassNameFromStackTrace();
        if (((TestClassTracker) trackerGet(className, false)).isTrackingClassParam()){
            trackerGet(className, false).addSetParam(param);
        } else {
            try {
                trackerGet(Utils.getTestClassAndMethodName()[1], true).addSetParam(param);
            } catch (IOException e) {
                // If catch IOException, it means that we are still in class-level setup method
                trackerGet(className, false).addSetParam(param);
            }
        }
    }

    /**
     * Get the set of parameters that have been used in the current test method
     * @param fullTestMethodName the name of the test method,
     *                           should be the full name with the format of "testClassName_testMethodName"
     * @return the set of parameters that have been used in the current test method
     */
    public static Set<String> getMethodUsedParams(String fullTestMethodName) {
        return (trackerGet(fullTestMethodName, true)).getUsedParams();
    }

    /**
     * Get the set of parameters that have been set in the current test method
     * @param fullTestMethodName the name of the test method,
     *                           should be the full name with the format of "testClassName_testMethodName"
     * @return the set of parameters that have been set in the current test method
     */
    public static Set<String> getMethodSetParams(String fullTestMethodName) {
        return (trackerGet(fullTestMethodName, true)).getSetParams();
    }

    /**
     * Get the set of parameters that have been used in the current test class
     * @return the set of parameters that have been used in the current test class
     */
    public static Set<String> getClassUsedParams(String className) {
        return (trackerGet(className, false)).getUsedParams();
    }

    /**
     * Get the set of parameters that have been set in the current test class
     * @return the set of parameters that have been set in the current test class
     */
    public static Set<String> getClassSetParams(String className) {
        return (trackerGet(className, false)).getSetParams();
    }


    /**
     * Get the set of all parameters that have been used in the current test class and method
     * @return the set of all parameters that have been used in the current test class and method
     */
    public static Set<String> getAllUsedParams(String className, String methodName) {
        String fullTestMethodName = Utils.getFullTestName(className, methodName);
        Set<String> allUsedParams = new HashSet<>();
        allUsedParams.addAll(getClassUsedParams(className));
        allUsedParams.addAll(getMethodUsedParams(fullTestMethodName));
        return allUsedParams;
    }

    /**
     * Get the set of all parameters that have been set in the current test class and method
     * @return the set of all parameters that have been set in the current test class and method
     */
    public static Set<String> getAllSetParams(String className, String methodName) {
        String fullTestName = Utils.getFullTestName(className, methodName);
        Set<String> allSetParams = new HashSet<>();
        allSetParams.addAll(getClassSetParams(className));
        allSetParams.addAll(getMethodSetParams(fullTestName));
        return allSetParams;
    }

    /**
     * Get the injected value of a config parameter
     */
    public static String getConfigParamValue(String param, String defaultValue) {
        Map<String, String> params = getInjectedParams();
        if (params.containsKey(param)) {
            return params.get(param);
        }
        return defaultValue;
    }

    /**
     * Inject config parameters into the configuration class
     * There are two ways to inject config parameters:
     * 1. From the command line: -DconfigInject="param1=value1,param2=value2";
     * 2. From a file: the file name is the test class name with the suffix ".json";
     * 3. From both the command line and a file: the command line injection would override the file injection
     * One can override this method to have different strategy to inject config parameters
     * into the configuration class
     * @param configSetterMethod the method to set config parameters
     * @param <T> the type of the config parameter
     */
    public static <T> void injectConfig(BiConsumer<String, T> configSetterMethod) {
        if (Options.mode != Modes.DEFAULT && Options.mode != Modes.INJECTING) {
            return;
        }
        if (injectFromFile) {
            try {
                injectFromFile(configSetterMethod);
            } catch (IOException e){
                Log.ERROR("Unable to inject config from file: " + e.getMessage());
            }
        }
        // The CLI injection would override the file injection for the common parameters
        injectFromCLI(configSetterMethod);
    }

    /**
     * Construct the map of injected configuration parameters
     */
    private static void constructInjectedParamMap() {
        if (Options.mode != Modes.DEFAULT && Options.mode != Modes.INJECTING) {
            return;
        }
        String currentTestClassName = Utils.inferTestClassNameFromStackTrace();
        Map<String, String> injectedParams = ((TestClassTracker) trackerGet(currentTestClassName, false)).getInjectedParams();
        if (injectFromFile) {
            try {
                File configFile = testClassToConfigFile.get(currentTestClassName);
                if (configFile != null) {
                    ConfigurationParser parser = new JsonConfigurationParser();
                    Map<String, String> configNameValueMap = parser.parseConfigNameValueMap(configFile.getAbsolutePath());
                    injectedParams.putAll(configNameValueMap);
                }
            } catch (IOException e){
                Log.ERROR("Unable to inject config from file: " + e.getMessage());
            }
        }
        // The CLI injection would override the file injection for the common parameters
        injectFromCLI((k, v) -> injectedParams.put(k, v.toString()));
    }

    /**
     * Get the map of injected configuration parameters
     */
    private static Map<String, String> getInjectedParams() {
        String currentTestClassName = Utils.inferTestClassNameFromStackTrace();
        Map<String, String> injectedParams = ((TestClassTracker) trackerGet(currentTestClassName, false)).getInjectedParams();
        if (Options.mode != Modes.DEFAULT && Options.mode != Modes.INJECTING) {
            return new HashMap<>();
        } else if (injectedParams.isEmpty()) {
            constructInjectedParamMap();
        }
        return injectedParams;
    }

    /**
     * This method can be directly added to the getter method for easier API instrumentation;
     * Every time the getter method is called, we check whether the current configruation object
     * is already injected with the config parameters based on the object id.
     * If not, we inject the config parameters.
     */
    public static <T> void injectConfig(int confObjectId, BiConsumer<String, T> configSetterMethod) {
/*
        Set<Integer> confObjectIds =
                ((TestMethodTracker) trackerGet(Utils.getTestClassAndMethodName()[1], true)).getConfObjectIds();
        if (confObjectIds.contains(confObjectId)) {
            return;
        }
        confObjectIds.add(confObjectId);
*/
        injectConfig(configSetterMethod);
    }

    /**
     * For a test with @Test annotation and ConfigTestRunner, the runner would be executed under @ConfigTrackStatement
     * and record the used parameters. This method would write the used parameters to a file.
     */
    public static void writeConfigToFile(String className, String methodName, String fileName) {
        methodName = Utils.getFullTestName(className, methodName);
        Utils.writeParamSetToJson(ConfigTracker.getAllUsedParams(className, methodName),
                ConfigTracker.getAllSetParams(className, methodName), new File(CONFIG_SAVE_DIR, fileName + ".json"));
    }

    // Internal methods

    /**
     * Inject config parameters from a file
     * @param configSetterMethod the method to set config parameters
     * @param <T> the type of the config parameter
     * @throws IOException if the configuration file cannot be read
     */
    private static <T> void injectFromFile(BiConsumer<String, T> configSetterMethod) throws IOException {
        String currentTestClassName = Utils.inferTestClassNameFromStackTrace();
        File configFile = testClassToConfigFile.get(currentTestClassName);
        if (configFile != null) {
            ConfigurationParser parser = new JsonConfigurationParser();
            Map<String, String> configNameValueMap = parser.parseConfigNameValueMap(configFile.getAbsolutePath());
            for (Map.Entry<String, String> entry : configNameValueMap.entrySet()) {
                configSetterMethod.accept(entry.getKey(), (T) entry.getValue());
            }
        }
    }

    /**
     * Inject config parameters from the command line
     * @param configSetterMethod the method to set config parameters
     * @param <T> the type of the config parameter
     */
    private static <T> void injectFromCLI(BiConsumer<String, T> configSetterMethod) {
        String paramMap = System.getProperty(CONFIG_CLI_INJECT_PROPERTY);
        if (paramMap != null) {
            String[] params = paramMap.split(",");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    configSetterMethod.accept(pair[0], (T) pair[1]);
                }
            }
        }
    }

    /**
     * Construct the map from test class name to the configuration file
     */
    private static boolean constructTestClzToConfigFileMap() {
        String configFileDir = System.getProperty(INJECT_CONFIG_FILE_DIR_PROPERTY);
        if (configFileDir == null) {
            return false;
        }
        File configFileDirFile = new File(configFileDir);
        if (!configFileDirFile.exists() || !configFileDirFile.isDirectory()) {
            throw new RuntimeException(INJECT_CONFIG_FILE_DIR_PROPERTY + ": " + configFileDir + " does not exist or is not a directory");
        }
        File[] configFiles = configFileDirFile.listFiles();
        if (configFiles != null) {
            for (File configFile : configFiles) {
                String configFileName = configFile.getName();
                String testClassName = configFileName.substring(0, configFileName.lastIndexOf("."));
                testClassToConfigFile.put(testClassName, configFile);
            }
        }
        return true;
    }

    /**
     * Update the config usage for the current test method
     */
    public static void updateConfigUsage(ConfigUsage configUsage, String className, String methodName) {
        String fullTestName = Utils.getFullTestName(className, methodName);
        configUsage.addClassLevelParams(getClassUsedParams(className));
        configUsage.addMethodLevelParams(fullTestName, getMethodUsedParams(fullTestName));
    }
}

