package edu.illinois;

import org.junit.runners.model.Statement;

import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class CTestJUnit4Statement extends Statement {
    private final Statement base;
    private final Set<String> params;

    public CTestJUnit4Statement(Statement base, Set<String> params) {
        this.base = base;
        this.params = params;
    }

    /**
     * This method is called when the test is executed.
     * Check whether all the specified parameters are used after the test execution.
     * @throws Throwable if the test execution throws an @UnUsedConfigParamException
     */
    @Override
    public void evaluate() throws Throwable {
        try {
            base.evaluate();
        } finally {
            if (Options.mode == Modes.CHECKING || Options.mode == Modes.DEFAULT) {
                for (String param : params) {
                    if (!ConfigTracker.isParameterUsed(param)) {
                        throw new UnUsedConfigParamException(param + " was not used during the test.");
                    }
                }
            }
        }
    }
}
