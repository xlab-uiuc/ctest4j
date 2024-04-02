package edu.illinois;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Author: Shuai Wang
 */
public class Ctest4jLogger {
    private static final Logger LOG = LoggerFactory.getLogger(Ctest4jLogger.class);

    private static String inferCurrentTestMethod() {
        String methodName;
        try {
            methodName = Utils.inferTestClassAndMethodNameFromStackTrace();
        } catch (IOException e) {
            methodName = "Unknown";
        }
        return methodName;
    }

    public static void logGet(String key) {
        LOG.info("[Ctest4j] Configuration-Get: " + key + " from " + inferCurrentTestMethod());
    }

    public static void logGet(String key, String value) {
        LOG.info("[Ctest4j] Configuration-Get: " + key + " = " + value + " from " + inferCurrentTestMethod());
    }

    public static void logSet(String key) {
        LOG.info("[Ctest4j] Configuration-Set: " + key + " from " + inferCurrentTestMethod());
    }

    public static void logSet(String key, String value) {
        LOG.info("[Ctest4j] Configuration-Set: " + key + " = " + value + " from " + inferCurrentTestMethod());
    }
}
