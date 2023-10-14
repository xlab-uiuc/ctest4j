package edu.illinois;

import edu.illinois.agent.ConfigRunnerAgent;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigTestRunner extends BlockJUnit4ClassRunner {
    private String configClassName;
    private List<String> getConfigMethodSignature;
    private List<String> setConfigMethodSignature;

    public ConfigTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        //getConfigMetadataAnnotation(klass);
    }

    /**
     * Get the ConfigMetadata annotation from the test class.
     * @param klass the test class.
     */
    private void getConfigMetadataAnnotation(Class<?> klass) {
        ConfigMetadata configMetadata = klass.getAnnotation(ConfigMetadata.class);
        if (configMetadata != null) {
            this.configClassName = configMetadata.configClassName();
            this.getConfigMethodSignature = List.of(configMetadata.getConfigMethodSignature());
            this.setConfigMethodSignature = List.of(configMetadata.setConfigMethodSignature());
            // If any of the above three fields is null, throw an exception
            if (configClassName == null || getConfigMethodSignature.isEmpty() || setConfigMethodSignature.isEmpty()) {
                throw new RuntimeException("ConfigMetadata annotation is not properly set.");
            }
            // Set to system properties
            System.setProperty("configClassName", configClassName);
            System.setProperty("getConfigMethodSignature", getConfigMethodSignature.toString());
            System.setProperty("setConfigMethodSignature", setConfigMethodSignature.toString());
            Log.INFO("From @ConfigMetadata Annotation", "Config Class Name: " + System.getProperty("configClassName"));
            Log.INFO("From @ConfigMetadata Annotation", "Get Config Method Signature: " + System.getProperty("getConfigMethodSignature"));
            Log.INFO("From @ConfigMetadata Annotation", "Set Config Method Signature: " + System.getProperty("setConfigMethodSignature"));
        } else {
            throw new RuntimeException("ConfigMetadata annotation is not set.");
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
            return new ConfigTestStatement(base, configTest.value());
        }
        return base;
    }
}

