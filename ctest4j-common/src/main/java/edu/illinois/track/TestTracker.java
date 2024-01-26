package edu.illinois.track;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Shuai Wang
 * Date:  1/5/24
 */
public class TestTracker {
    /** The set of parameters that have been used in the current test class */
    protected final Set<String> usedParams = Collections.synchronizedSet(new HashSet<>());
    /** The set of parameters that have been set in the current test class */
    protected final Set<String> setParams = Collections.synchronizedSet(new HashSet<>());
    /** The name of the current test class */

    public TestTracker() {}

    public Set<String> getUsedParams() {
        return usedParams;
    }

    public Set<String> getSetParams() {
        return setParams;
    }

    public void addUsedParam(String param) {
        usedParams.add(param);
    }

    public void addSetParam(String param) {
        setParams.add(param);
    }

    public void clearUsedParams() {
        usedParams.clear();
    }

    public void clearSetParams() {
        setParams.clear();
    }

    public boolean isParameterUsed(String param) {
        return usedParams.contains(param);
    }

    public boolean isParameterSet(String param) {
        return setParams.contains(param);
    }
}


