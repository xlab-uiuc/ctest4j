package edu.illinois;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static edu.illinois.Names.TRACKING_LOG_PREFIX;
import static edu.illinois.Options.saveUsedParamToFile;
import static edu.illinois.Utils.getTestMethodFullName;

/**
 * Author: Shuai Wang
 * Date:  10/15/23
 */
@Deprecated
public class ConfigTrackJUnit4Statement extends Statement {
    private final Statement base;
    private final FrameworkMethod method;

    public ConfigTrackJUnit4Statement(Statement base, FrameworkMethod method) {
        this.base = base;
        this.method = method;
    }

    @Override
    public void evaluate() throws Throwable {
        try {
            base.evaluate();
        } finally {
            String className = method.getDeclaringClass().getName();
            String methodName = method.getName();
            Log.INFO(TRACKING_LOG_PREFIX, className + "#" + methodName,
                    "uses configuration parameters: " + ConfigTracker.getAllUsedParams(className, methodName) + " and set parameters: " +
                    ConfigTracker.getAllSetParams(className, methodName));
            if (saveUsedParamToFile) {
                ConfigTracker.writeConfigToFile(className, methodName, getTestMethodFullName(method));
            }
        }
    }
}
