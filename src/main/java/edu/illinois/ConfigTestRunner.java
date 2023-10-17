package edu.illinois;

import edu.illinois.agent.ConfigRunnerAgent;
import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.*;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigTestRunner extends BlockJUnit4ClassRunner {
    private final Set<String> classLevelParameters;

    public ConfigTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        // Retrieve class-level parameters if present
        ConfigTestClass configTestClassAnnotation = klass.getAnnotation(ConfigTestClass.class);
        if (configTestClassAnnotation != null) {
            classLevelParameters = new HashSet<>(Arrays.asList(configTestClassAnnotation.value()));
        } else {
            classLevelParameters = new HashSet<>();
        }
    }

    /**
     * Get all the test methods with @Test and @ConfigTest annotations.
     * @return a list of test methods.
     */
    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        // Return both methods with @Test and @ConfigTest annotations
        List<FrameworkMethod> methods = new ArrayList<>();
        methods.addAll(super.computeTestMethods());
        methods.addAll(getTestClass().getAnnotatedMethods(ConfigTest.class));
        return Collections.unmodifiableList(methods);
    }

    /**
     * Invoke the configuration tests with @ConfigTest annotation.
     * @param method
     * @param test
     * @return ConfigTestStatement if the method has @ConfigTest annotation,
     * otherwise return the original statement.
     */
    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement base = super.methodInvoker(method, test);
        ConfigTest configTest = method.getAnnotation(ConfigTest.class);
        if (configTest != null) {
            Set<String> methodLevelParameters = new HashSet<>();
            methodLevelParameters.addAll(Arrays.asList(configTest.value()));
            // add class level parameters to method level parameters
            methodLevelParameters.addAll(classLevelParameters);
            return new ConfigTestStatement(base, methodLevelParameters);
        }
        Test testAnnotation = method.getAnnotation(Test.class);
        if (testAnnotation != null) {
            return new ConfigTrackStatement(base, method);
        }
        return base;
    }
}

