package edu.illinois.junit4;

import edu.illinois.ConfigTracker;
import edu.illinois.Modes;
import edu.illinois.Options;
import edu.illinois.UnUsedConfigParamException;
import org.junit.runners.model.Statement;

import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class CTestStatement extends Statement {
    private final Statement base;
    private final Set<String> params;

    public CTestStatement(Statement base, Set<String> params) {
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
        ConfigTracker.startTest();
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
