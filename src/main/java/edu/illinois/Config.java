package edu.illinois;

import org.objectweb.asm.Opcodes;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class Config {
    public static final int JAVA_VERSION = getJavaVersion();
    // TODO: change this to the correct ASM version
    public static final int ASMVersion = Opcodes.ASM5;

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
