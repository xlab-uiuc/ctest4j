package edu.illinois;
import edu.illinois.instrument.ConfigClassAdapter;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigTestRunner extends BlockJUnit4ClassRunner {
    private String configClassName;

    public ConfigTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        ConfigMetadata configMetadata = klass.getAnnotation(ConfigMetadata.class);
        this.configClassName = configMetadata.configClassName();
        instrumentConfigurationClass();
    }

    private void instrumentConfigurationClass() {
        try {
            ClassReader reader = new ClassReader(configClassName);
            ClassWriter writer = new ClassWriter(reader, 0);
            ConfigClassAdapter adapter = new ConfigClassAdapter(writer);
            reader.accept(adapter, 0);
            byte[] byteCode = writer.toByteArray();
            // This assumes you have a mechanism to use the modified bytecode
            // Typically, you'd use a custom ClassLoader here to load this modified class
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

