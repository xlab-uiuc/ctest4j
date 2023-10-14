package edu.illinois;

import java.util.Set;
import java.util.HashSet;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigTracker {
    private static final ThreadLocal<Set<String>> usedParams = ThreadLocal.withInitial(HashSet::new);

    public static void startTest() {
        System.out.println("Starting a new test.");
        usedParams.get().clear();
    }

    public static boolean isParameterUsed(String param) {
        return usedParams.get().contains(param);
    }

    public static void markParamAsUsed(String param) {
        usedParams.get().add(param);
    }
}

