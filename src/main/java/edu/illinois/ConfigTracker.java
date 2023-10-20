package edu.illinois;

import edu.illinois.parser.ConfigurationParser;
import edu.illinois.parser.JsonConfigurationParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.BiConsumer;

import static edu.illinois.Names.CONFIG_CLI_INJECT_PROPERTY;
import static edu.illinois.Names.CONFIG_FILE_DIR_PROPERTY;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigTracker {
    /** The set of parameters that have been used in the current test method */
    private static final ThreadLocal<Set<String>> usedParams = ThreadLocal.withInitial(HashSet::new);
    /** The name of the current test class */
    private static String currentTestClassName = null;
    /** The map from test class name to the configuration file */
    private static final Map<String, File> testClassToConfigFile = new HashMap<>();
    /** Whether to inject config parameters from a file */
    private static final boolean injectFromFile;

    static {
        injectFromFile = constructTestClzToConfigFileMap();
    }

    /**
     * Start a new test method, clear the set of used parameters
     */
    public static void startTest() {
        usedParams.get().clear();
    }

    /**
     * Check whether a parameter has been used in the current test method
     * @param param the parameter to check
     * @return true if the parameter has been used, false otherwise
     */
    public static boolean isParameterUsed(String param) {
        return usedParams.get().contains(param);
    }

    /**
     * Mark a parameter as used in the current test method
     * @param param the parameter to mark
     */
    public static void markParamAsUsed(String param) {
        usedParams.get().add(param);
    }

    /**
     * Get the set of parameters that have been used in the current test method
     * @return the set of parameters that have been used in the current test method
     */
    public static Set<String> getUsedParams() {
        return usedParams.get();
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
    public static <T> void injectConfig(BiConsumer<String, T> configSetterMethod) throws IOException {
        if (injectFromFile) {
            injectFromFile(configSetterMethod);
        }
        // The CLI injection would override the file injection for the common parameters
        injectFromCLI(configSetterMethod);
        System.out.println(ConfigTracker.getCurrentTestClassName());
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
        String configFileDir = System.getProperty(CONFIG_FILE_DIR_PROPERTY);
        if (configFileDir == null) {
            return false;
        }
        File configFileDirFile = new File(configFileDir);
        if (!configFileDirFile.exists() || !configFileDirFile.isDirectory()) {
            throw new RuntimeException(CONFIG_FILE_DIR_PROPERTY + ": " + configFileDir + " does not exist or is not a directory");
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
}

