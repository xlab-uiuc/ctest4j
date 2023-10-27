package edu.illinois;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Shuai Wang
 * Date:  10/18/23
 */
public class CTestSuite extends Suite {

    public CTestSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
    }

    /**
     * For every test class specified in @SuiteClasses annotation, create a ConfigTestRunner.
     * @return a list of ConfigTestRunner.
     */
    @Override
    protected List<Runner> getChildren() {
        List<Class<?>> failedKlasses = new ArrayList<>();
        List<Runner> runners = new ArrayList<>();
        SuiteClasses annotation = getTestClass().getJavaClass().getAnnotation(SuiteClasses.class);
        if (annotation != null) {
            for (Class<?> testClass : annotation.value()) {
                // Skip abstract classes
                if (Modifier.isAbstract(testClass.getModifiers())) {
                    continue;
                }
                try {
                    runners.add(new CTestRunner(testClass));
                } catch (InitializationError initializationError) {
                    failedKlasses.add(testClass);
                }
            }
        }
        Log.INFO("Failed to initialize the following classes: " + failedKlasses);
        return runners;
    }
}
