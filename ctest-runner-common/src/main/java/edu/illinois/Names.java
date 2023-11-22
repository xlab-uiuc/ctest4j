package edu.illinois;

import org.objectweb.asm.Opcodes;

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
    public static final String TRACKER_METHOD_SIGNATURE = "(Ljava/lang/String;)V";
    public static final String CONFIG_FILE_TYPE_PROPERTY = "config.file.type";
    public static final String TRACKING_LOG_PREFIX = "[ConfigTestRunner]TEST";
    /** The directory of configuration files that stores the value to be injected in the test */
    public static final String INJECT_CONFIG_FILE_DIR_PROPERTY = "config.inject.dir";
    /** The command-line argument that specifies the configuration value to be injected */
    public static final String CONFIG_CLI_INJECT_PROPERTY = "config.inject.cli";
    /** The directory of files that stores the used configuration parameters by each test */
    public static final String CONFIG_MAPPING_DIR = System.getProperty("ctest.mapping.dir", "ctest/mapping");
    /** The seperator between test class name and test method name */
    public static final String TEST_CLASS_METHOD_SEPERATOR = "_";
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
