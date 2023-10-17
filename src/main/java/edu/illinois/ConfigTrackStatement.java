package edu.illinois;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.awt.*;

/**
 * Author: Shuai Wang
 * Date:  10/15/23
 */
public class ConfigTrackStatement extends Statement {
    private final Statement base;
    private final FrameworkMethod method;

    public ConfigTrackStatement(Statement base, FrameworkMethod method) {
        this.base = base;
        this.method = method;
    }

    @Override
    public void evaluate() throws Throwable {
        ConfigTracker.startTest();
        try {
            base.evaluate();
        } finally {
            Log.INFO("Test", method.getName(), "uses configuration parameters: " + ConfigTracker.getUsedParams());
        }
    }
}