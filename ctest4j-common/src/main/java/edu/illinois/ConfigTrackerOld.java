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
@Deprecated
public class ConfigTrackerOld {
    private static boolean trackClassParam = false;
    /** The set of parameters that have been used in the current test class */
    private static final Set<String> classUsedParams = Collections.synchronizedSet(new HashSet<>());
    /** The set of parameters that have been set in the current test class */
    private static final Set<String> classSetParams = Collections.synchronizedSet(new HashSet<>());
    /** The set of parameters that have been used in the current test method */
    private static final Set<String> methodUsedParams = Collections.synchronizedSet(new HashSet<>());
    /** The set of parameters that have been set in the current test method */
    private static final Set<String> methodSetParams = Collections.synchronizedSet(new HashSet<>());
    /** The name of the current test class */
    private static String currentTestClassName = null;
    /** The map from test class name to the configuration file */
    private static final Map<String, File> testClassToConfigFile = new ConcurrentHashMap<>();
    /** Whether to inject config parameters from a file */
    private static final boolean injectFromFile;
    /** The set of configuration object ids that already done the injection */
    private static Set<Integer> confObjectIds = Collections.synchronizedSet(new HashSet<>());

    /** The configuration parameters key-value pairs that will be injected */
    private static final Map<String, String> injectedParams = new ConcurrentHashMap<>();

    static {
        injectFromFile = constructTestClzToConfigFileMap();
    }

    /**
     * Start tracking class-level parameters
     */
    public static void startTestClass() {
        trackClassParam = true;
        classUsedParams.clear();
        classSetParams.clear();
    }

    /**
     * Start a new test method, clear the set of used parameters
     */
    public static void startTestMethod() {
        // Stop tracking class-level parameters once a test method is started
        trackClassParam = false;
        methodUsedParams.clear();
        methodSetParams.clear();
    }

    /**
     * Check whether a parameter has been used in the current test method or class
     * @param param the parameter to check
     * @return true if the parameter has been used, false otherwise
     */
    public static boolean isParameterUsed(String param) {
        return methodUsedParams.contains(param) || classUsedParams.contains(param);
    }

    /**
     * Mark a parameter as used in the current test method or class
     * @param param the parameter to mark
     */
    public static void markParamAsUsed(String param) {
        if (trackClassParam) {
            classUsedParams.add(param);
        } else {
            methodUsedParams.add(param);
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
     * Check whether a parameter has been set in the current test method or class
     * @param param the parameter to check
     * @return true if the parameter has been set, false otherwise
     */
    public static boolean isParameterSet(String param) {
        return methodSetParams.contains(param) || classSetParams.contains(param);
    }

    /**
     * Mark a parameter as set in the current test method or class
     * @param param the parameter that has been set
     */
    public static void markParamAsSet(String param) {
        if (trackClassParam) {
            classSetParams.add(param);
        } else {
            methodSetParams.add(param);
        }
    }

    /**
     * Get the set of parameters that have been used in the current test method
     * @return the set of parameters that have been used in the current test method
     */
    public static Set<String> getMethodUsedParams() {
        return methodUsedParams;
    }

    /**
     * Get the set of parameters that have been set in the current test method
     * @return the set of parameters that have been set in the current test method
     */
    public static Set<String> getSetParams() {
        return methodSetParams;
    }


    /**
     * Get the set of parameters that have been used in the current test class
     * @return the set of parameters that have been used in the current test class
     */
    public static Set<String> getClassUsedParams() {
        return classUsedParams;
    }

    /**
     * Get the set of parameters that have been set in the current test class
     * @return the set of parameters that have been set in the current test class
     */
    public static Set<String> getClassSetParams() {
        return classSetParams;
    }


    /**
     * Get the set of all parameters that have been used in the current test class and method
     * @return the set of all parameters that have been used in the current test class and method
     */
    public static Set<String> getAllUsedParams() {
        Set<String> allUsedParams = new HashSet<>();
        allUsedParams.addAll(methodUsedParams);
        allUsedParams.addAll(classUsedParams);
        return allUsedParams;
    }

    /**
     * Get the set of all parameters that have been set in the current test class and method
     * @return the set of all parameters that have been set in the current test class and method
     */
    public static Set<String> getAllSetParams() {
        Set<String> allSetParams = new HashSet<>();
        allSetParams.addAll(methodSetParams);
        allSetParams.addAll(classSetParams);
        return allSetParams;
    }

    /**
     * Set the name of the current test class
     * @param testName the name of the current test class
     */
    public static void setCurrentTestClassName(String testName) {
        currentTestClassName = testName;
    }

    /**
     * Get the name of the current test class
     * @return the name of the current test class
     */
    public static String getCurrentTestClassName() {
        return currentTestClassName;
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
        if (confObjectIds.contains(confObjectId)) {
            return;
        }
        confObjectIds.add(confObjectId);
        injectConfig(configSetterMethod);
    }

    /**
     * For a test with @Test annotation and ConfigTestRunner, the runner would be executed under @ConfigTrackStatement
     * and record the used parameters. This method would write the used parameters to a file.
     */
    public static void writeConfigToFile(String fileName) {
        Utils.writeParamSetToJson(ConfigTrackerOld.getAllUsedParams(), ConfigTrackerOld.getAllSetParams(), new File(CONFIG_SAVE_DIR, fileName + ".json"));
    }

    // Internal methods

    /**
     * Inject config parameters from a file
     * @param configSetterMethod the method to set config parameters
     * @param <T> the type of the config parameter
     * @throws IOException if the configuration file cannot be read
     */
    private static <T> void injectFromFile(BiConsumer<String, T> configSetterMethod) throws IOException {
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
    public static void updateConfigUsage(ConfigUsage configUsage, String methodName) {
        configUsage.addMethodLevelParams(methodName, methodUsedParams);
        configUsage.addClassLevelParams(classUsedParams);
    }
}

