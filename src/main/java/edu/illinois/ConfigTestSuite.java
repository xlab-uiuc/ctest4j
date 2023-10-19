package edu.illinois;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Shuai Wang
 * Date:  10/18/23
 */
public class ConfigTestSuite extends Suite {

    public ConfigTestSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
    }

    /**
     * For every test class specified in @SuiteClasses annotation, create a ConfigTestRunner.
     * @return a list of ConfigTestRunner.
     */
    @Override
    protected List<Runner> getChildren() {
        List<Runner> runners = new ArrayList<>();
        SuiteClasses annotation = getTestClass().getJavaClass().getAnnotation(SuiteClasses.class);
        if (annotation != null) {
            for (Class<?> testClass : annotation.value()) {
                try {
                    runners.add(new ConfigTestRunner(testClass));
                } catch (InitializationError initializationError) {
                    throw new RuntimeException("Unable to initialize ConfigTestRunner", initializationError);
                }
            }
        }
        return runners;
    }
}
