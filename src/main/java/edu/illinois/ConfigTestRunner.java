package edu.illinois;

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
    private String getConfigMethodSignature;
    private String setConfigMethodSignature;

    public ConfigTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        getConfigMetadataAnnotation(klass);
        //getCustomizedClassLoader();
    }

    /**
     * Get the ConfigMetadata annotation from the test class.
     * @param klass
     */
    private void getConfigMetadataAnnotation(Class<?> klass) {
        ConfigMetadata configMetadata = klass.getAnnotation(ConfigMetadata.class);
        if (configMetadata != null) {
            this.configClassName = configMetadata.configClassName();
            this.getConfigMethodSignature = configMetadata.getConfigMethodSignature();
            this.setConfigMethodSignature = configMetadata.setConfigMethodSignature();
            // If any of the above three fields is null, throw an exception
            if (configClassName == null || getConfigMethodSignature == null || setConfigMethodSignature == null) {
                throw new RuntimeException("ConfigMetadata annotation is not properly set.");
            }
        } else {
            throw new RuntimeException("ConfigMetadata annotation is not set.");
        }
    }

    /**
     * Instrument the configuration class for tracking the usage of configuration parameters.
     */
/*    private ClassLoader getCustomizedClassLoader() {
        try {
            ClassReader reader = new ClassReader(configClassName);
            ClassWriter writer = new ClassWriter(reader, 0);
            ConfigClassAdapter adapter = new ConfigClassAdapter(writer);
            reader.accept(adapter, 0);
            byte[] byteCode = writer.toByteArray();

            return new ConfigClassLoader(configClassName, byteCode, getClass().getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    protected Object createTest() throws Exception {
        // Set the customized class loader
        //Thread.currentThread().setContextClassLoader(getCustomizedClassLoader());
        System.out.println("Class loader is " + Thread.currentThread().getContextClassLoader());
        // print thread id
        System.out.println("Thread id: " + Thread.currentThread().getId());
        return getTestClass().getJavaClass().newInstance();
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

