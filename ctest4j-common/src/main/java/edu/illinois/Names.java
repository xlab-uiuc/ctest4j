package edu.illinois;

import org.objectweb.asm.Opcodes;

import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class Names {
    public static final int JAVA_VERSION = getJavaVersion();
    // TODO: change this to the correct ASM version
    public static final int ASMVersion = Opcodes.ASM5;
    public static final String AGENT_MODE = System.getProperty("agent.mode", "JUNIT"); // JUNIT or RUNNER

    public static final String TRACKER_CLASS_NAME = "edu/illinois/ConfigTracker";
    public static final String TRACKER_METHOD_NAME = "markParamAsUsed";
    public static final String CTEST_MODE_PROPERTY = "ctest.mode";
    public static final String TRACKER_METHOD_SIGNATURE = "(Ljava/lang/String;)V";
    public static final String CONFIG_FILE_TYPE_PROPERTY = "config.file.type";
    public static final String TRACKING_LOG_PREFIX = "[CTEST-RUNNER] TEST";
    /** The directory of configuration files that stores the value to be injected in the test */
    public static final String INJECT_CONFIG_FILE_DIR_PROPERTY = "config.inject.dir";
    /** The command-line argument that specifies the configuration value to be injected */
    public static final String CONFIG_CLI_INJECT_PROPERTY = "config.inject.cli";
    /** The property that contains the target parameters to be tested, seperated by comma */
    public static final String CTEST_SELECTION_PARAMETER_PROPERTY = "ctest.selection.parameter";
    public static final String CTEST_RUNTIME_SELECTION_PROPERTY = "ctest.runtime.selection";
    public static final Boolean CTEST_RUNTIME_SELECTION = Boolean.valueOf(System.getProperty(CTEST_RUNTIME_SELECTION_PROPERTY, "false"));
    public static final String CTEST_MAPPING_DIR_PROPERTY = "ctest.mapping.dir";
    /** The directory of files that stores the used configuration parameters by each test */
    public static final String CONFIG_MAPPING_DIR = System.getProperty(CTEST_MAPPING_DIR_PROPERTY, "ctest/mapping");
    public static final String CTEST_CONFIG_SAVE_DIR_PROPERTY = "ctest.config.save.dir";
    public static final String CONFIG_SAVE_DIR = System.getProperty(CTEST_CONFIG_SAVE_DIR_PROPERTY, "ctest/saved_mapping");
    public static final String CONFIG_SAVE_PROPERTY = "ctest.config.save";
    public static final String CONFIG_EXCLUDE_PROPERTY = "ctest.config.exclude";
    public static final String CONFIG_EXCLUDE_SEPARATOR = ",";
    /** The separator between test class name and test method name */
    public static final String TEST_CLASS_METHOD_SEPARATOR = "_";
    /** The separator between process id and thread id */
    public static final String PID_TID_SEPARATOR = "@";
    /** The name of maven surefire plugin */
    public static final String SUREFIRE_PLUGIN_KEY = "org.apache.maven.plugins:maven-surefire-plugin";
    /** The method to get the value of the configuration parameter */
    public static final String CTEST_GETTER = "ctest.getter";
    /** The method to set the value of the configuration parameter */
    public static final String CTEST_SETTER = "ctest.setter";
    /** The method to inject the value of the configuration parameter */
    public static final String CTEST_INJECTER = "ctest.injecter";
    public static final Set<String> CTEST_RUNNER_PROPERTY_LIST = Set.of(
            CTEST_MODE_PROPERTY,
            INJECT_CONFIG_FILE_DIR_PROPERTY,
            CONFIG_CLI_INJECT_PROPERTY,
            CTEST_SELECTION_PARAMETER_PROPERTY,
            CTEST_RUNTIME_SELECTION_PROPERTY,
            CTEST_MAPPING_DIR_PROPERTY,
            CTEST_CONFIG_SAVE_DIR_PROPERTY,
            CONFIG_SAVE_PROPERTY
    );

    /**
     * Check if the property is a ctest-runner property
     */
    public static boolean isCTestRunnerProperty(String propertyName) {
        return CTEST_RUNNER_PROPERTY_LIST.contains(propertyName);
    }

    /**
     * Get the ASM version based on the java version
     * @param javaVersion java version
     * @return ASM version
     */
    private static int getASMVersion (int javaVersion) {
        if (javaVersion == 8) {
            return Opcodes.ASM5;
        } else if (javaVersion >= 9) {
            return Opcodes.ASM9;
        } else {
            return -1;
        }
    }

    /**
     * Get the java version
     * @return java version
     */
    private static int getJavaVersion() {
        String javaSpecVersion = System.getProperty("java.specification.version");

        if ("1.8".equals(javaSpecVersion)) {
            return 8;
        } else {
            try {
                int versionNumber = Integer.parseInt(javaSpecVersion);
                if (versionNumber >= 9) {
                    return 9;
                } else {
                    return -1;
                }
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }
}
