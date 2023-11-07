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
public class ConfigTrackJUnit4Statement extends Statement {
    private final Statement base;
    private final FrameworkMethod method;

    public ConfigTrackJUnit4Statement(Statement base, FrameworkMethod method) {
        this.base = base;
        this.method = method;
    }

    @Override
    public void evaluate() throws Throwable {
        //ConfigTracker.startTestMethod();
        try {
            base.evaluate();
        } finally {
            Log.INFO(TRACKING_LOG_PREFIX, method.getDeclaringClass().getCanonicalName() + "#" + method.getName(),
                    "uses configuration parameters: " + ConfigTracker.getAllUsedParams() + " and set parameters: " +
                    ConfigTracker.getAllSetParams());
            if (saveUsedParamToFile) {
                ConfigTracker.writeConfigToFile(getTestMethodFullName(method));
            }
        }
    }
}
