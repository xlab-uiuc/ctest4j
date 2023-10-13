package edu.illinois;

import org.junit.runners.model.Statement;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigTestStatement extends Statement {
    private final Statement base;
    private final String[] params;

    public ConfigTestStatement(Statement base, String[] params) {
        this.base = base;
        this.params = params;
    }

    @Override
    public void evaluate() throws Throwable {
        ConfigTracker.startTest();
        try {
            base.evaluate();
        } finally {
            for (String param : params) {
                if (!ConfigTracker.isParameterUsed(param)) {
                    throw new UnUsedConfigParamException(param + " was not used during the test.");
                }
            }
        }
    }
}
