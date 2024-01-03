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
    private static boolean trackClassParam = false;
    /** The set of parameters that have been used in the current test class */
    //private static final Set<String> classUsedParams = Collections.synchronizedSet(new HashSet<>());
    private static final ThreadLocal<Set<String>> classUsedParams = ThreadLocal.withInitial(HashSet::new);
    /** The set of parameters that have been set in the current test class */
    //private static final Set<String> classSetParmas = Collections.synchronizedSet(new HashSet<>());
    private static final ThreadLocal<Set<String>> classSetParams = ThreadLocal.withInitial(HashSet::new);
    /** The set of parameters that have been used in the current test method */
    //private static final Set<String> methodUsedParams = Collections.synchronizedSet(new HashSet<>());
    private static final ThreadLocal<Set<String>> methodUsedParams = ThreadLocal.withInitial(HashSet::new);
    /** The set of parameters that have been set in the current test method */
    //private static final Set<String> methodSetParmas = Collections.synchronizedSet(new HashSet<>());
    private static final ThreadLocal<Set<String>> methodSetParams = ThreadLocal.withInitial(HashSet::new);
    /** The name of the current test class */
    private static String currentTestClassName = null;
    /** The map from test class name to the configuration file */
    private static final Map<String, File> testClassToConfigFile = new ConcurrentHashMap<>();
    /** Whether to inject config parameters from a file */
    private static final boolean injectFromFile;
    /** The set of configuration object ids that already done the injection */
    //private static Set<Integer> confObjectIds = Collections.synchronizedSet(new HashSet<>());
    private static final ThreadLocal<Set<Integer>> confObjectIds = ThreadLocal.withInitial(HashSet::new);

    /** The configuration parameters key-value pairs that will be injected */
    private static final ThreadLocal<Map<String, String>> injectedParams = ThreadLocal.withInitial(HashMap::new);

    static {
        injectFromFile = constructTestClzToConfigFileMap();
    }

    /**
     * Start tracking class-level parameters
     */
    public static void startTestClass() {
        trackClassParam = true;
        classUsedParams.get().clear();
        classSetParams.get().clear();
    }

    /**
     * Start a new test method, clear the set of used parameters
     */
    public static void startTestMethod() {
        // Stop tracking class-level parameters once a test method is started
        trackClassParam = false;
        methodUsedParams.get().clear();
        methodSetParams.get().clear();
    }

    /**
     * Check whether a parameter has been used in the current test method or class
     * @param param the parameter to check
     * @return true if the parameter has been used, false otherwise
     */
    public static boolean isParameterUsed(String param) {
        return methodUsedParams.get().contains(param) || classUsedParams.get().contains(param);
    }

    /**
     * Mark a parameter as used in the current test method or class
     * @param param the parameter to mark
     */
    public static void markParamAsUsed(String param) {
        if (trackClassParam) {
            classUsedParams.get().add(param);
        } else {
            methodUsedParams.get().add(param);
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
        return methodSetParams.get().contains(param) || classSetParams.get().contains(param);
    }

    /**
     * Mark a parameter as set in the current test method or class
     * @param param the parameter that has been set
     */
    public static void markParamAsSet(String param) {
        if (trackClassParam) {
            classSetParams.get().add(param);
        } else {
            methodSetParams.get().add(param);
        }
    }

    /**
     * Get the set of parameters that have been used in the current test method
     * @return the set of parameters that have been used in the current test method
     */
    public static Set<String> getMethodUsedParams() {
        return methodUsedParams.get();
    }

    /**
     * Get the set of parameters that have been set in the current test method
     * @return the set of parameters that have been set in the current test method
     */
    public static Set<String> getSetParams() {
        return methodSetParams.get();
    }


    /**
     * Get the set of parameters that have been used in the current test class
     * @return the set of parameters that have been used in the current test class
     */
    public static Set<String> getClassUsedParams() {
        return classUsedParams.get();
    }

    /**
     * Get the set of parameters that have been set in the current test class
     * @return the set of parameters that have been set in the current test class
     */
    public static Set<String> getClassSetParams() {
        return classSetParams.get();
    }


    /**
     * Get the set of all parameters that have been used in the current test class and method
     * @return the set of all parameters that have been used in the current test class and method
     */
    public static Set<String> getAllUsedParams() {
        Set<String> allUsedParams = new HashSet<>();
        allUsedParams.addAll(methodUsedParams.get());
        allUsedParams.addAll(classUsedParams.get());
        return allUsedParams;
    }

    /**
     * Get the set of all parameters that have been set in the current test class and method
     * @return the set of all parameters that have been set in the current test class and method
     */
    public static Set<String> getAllSetParams() {
        Set<String> allSetParams = new HashSet<>();
        allSetParams.addAll(methodSetParams.get());
        allSetParams.addAll(classSetParams.get());
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
                    injectedParams.get().putAll(configNameValueMap);
                }
            } catch (IOException e){
                Log.ERROR("Unable to inject config from file: " + e.getMessage());
            }
        }
        // The CLI injection would override the file injection for the common parameters
        injectFromCLI((k, v) -> injectedParams.get().put(k, v.toString()));
    }

    /**
     * Get the map of injected configuration parameters
     */
    private static Map<String, String> getInjectedParams() {
        if (Options.mode != Modes.DEFAULT && Options.mode != Modes.INJECTING) {
            return new HashMap<>();
        } else if (injectedParams.get().isEmpty()) {
            constructInjectedParamMap();
        }
        return injectedParams.get();
    }

    /**
     * This method can be directly added to the getter method for easier API instrumentation;
     * Every time the getter method is called, we check whether the current configruation object
     * is already injected with the config parameters based on the object id.
     * If not, we inject the config parameters.
     */
    public static <T> void injectConfig(int confObjectId, BiConsumer<String, T> configSetterMethod) {
        if (confObjectIds.get().contains(confObjectId)) {
            return;
        }
        confObjectIds.get().add(confObjectId);
        injectConfig(configSetterMethod);
    }

    /**
     * For a test with @Test annotation and ConfigTestRunner, the runner would be executed under @ConfigTrackStatement
     * and record the used parameters. This method would write the used parameters to a file.
     */
    public static void writeConfigToFile(String fileName) {
        Utils.writeParamSetToJson(ConfigTracker.getAllUsedParams(), ConfigTracker.getAllSetParams(), new File(CONFIG_SAVE_DIR, fileName + ".json"));
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
        configUsage.addMethodLevelParams(methodName, methodUsedParams.get());
        configUsage.addClassLevelParams(classUsedParams.get());
    }
}

