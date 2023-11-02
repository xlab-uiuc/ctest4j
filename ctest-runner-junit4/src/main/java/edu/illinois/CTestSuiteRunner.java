package edu.illinois;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.IOException;

import static edu.illinois.Options.ctestSuiteTracking;

/**
 * Author: Shuai Wang
 * Date:  10/27/23
 */
public class CTestSuiteRunner extends CTestRunner {

    public CTestSuiteRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement base = vanillaMethodInvoker(method, test);
        if (ctestSuiteTracking) {
            return super.methodInvoker(method, test);
        } else {
            try {
                return new CTestStatement(base, getRequiredParametersFromDefaultFile(method));
            } catch (IOException e) {
                throw new RuntimeException("Failed to run CTest " + method.getName() + " from CTestSuite ");
            }
        }
    }
}
