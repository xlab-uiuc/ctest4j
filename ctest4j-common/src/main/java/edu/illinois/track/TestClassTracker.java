package edu.illinois.track;

import edu.illinois.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Shuai Wang
 * Date:  1/5/24
 */
public class TestClassTracker extends TestTracker {
    private boolean trackClassParam = true;
    private final String testClassName;
    /** The configuration parameters key-value pairs that will be injected */
    private final Map<String, String> injectedParams = new ConcurrentHashMap<>();

    public TestClassTracker() {
        super();
        testClassName = Utils.inferTestClassNameFromStackTrace();
    }

    public String getTestClzName() {
        return testClassName;
    }

    public void startTrackingClassParam() {
        trackClassParam = true;
    }

    public void stopTrackingClassParam() {
        trackClassParam = false;
    }

    public boolean isTrackingClassParam() {
        return trackClassParam;
    }

    public Map<String, String> getInjectedParams() {
        return injectedParams;
    }

    public void addInjectedParam(String key, String value) {
        injectedParams.put(key, value);
    }

    public void clearInjectedParams() {
        injectedParams.clear();
    }

    public void addAllInjectedParams(Map<String, String> params) {
        injectedParams.putAll(params);
    }
}
