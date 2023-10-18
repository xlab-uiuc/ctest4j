package edu.illinois;

import edu.illinois.parser.ConfigurationParser;
import edu.illinois.parser.JsonConfigurationParser;
import edu.illinois.parser.NullConfigurationParser;
import edu.illinois.parser.XmlConfigurationParser;
import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
            try {
                classLevelParameters = getAllClassParameters(configTestClassAnnotation);
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse configuration file from class " + klass.getName() + " Annotation", e);
            }
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
            try {
                return new ConfigTestStatement(base, getAllMethodParameters(configTest));
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse configuration file from method " + method.getName() + " Annotation", e);
            }
        }
        Test testAnnotation = method.getAnnotation(Test.class);
        if (testAnnotation != null) {
            return new ConfigTrackStatement(base, method);
        }
        return base;
    }

    /**
     * @ConfigTest would override the expected exception from @Test annotation.
     */
    @Override
    protected Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
        // From @ConfigTest annotation
        ConfigTest configTest = method.getAnnotation(ConfigTest.class);
        if (configTest != null) {
            Class<? extends Throwable> configTestExpectedException = configTest.expected();
            if (configTestExpectedException != ConfigTest.None.class) {
                return new ExpectException(next, configTestExpectedException);
            }
        }
        // From @Test annotation
        Test testAnnotation = method.getAnnotation(Test.class);
        if (testAnnotation != null) {
            Class<? extends Throwable> testExpectedException = testAnnotation.expected();
            if (testExpectedException != Test.None.class) {
                return new ExpectException(next, testExpectedException);
            }

        }
        return next;
    }

    /**
     * @ConfigTest would override the timeout from @Test annotation.
     */
    @Override
    protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
        long timeout = 0;
        ConfigTest configTest = method.getAnnotation(ConfigTest.class);
        if (configTest != null) {
            timeout = configTest.timeout();
        }
        return FailOnTimeout.builder()
                .withTimeout(timeout, TimeUnit.MILLISECONDS)
                .build(next);
    }

    /**
     * Get the parameters from a configuration file.
     * @param file the path of the configuration file
     * @return the set of parameters that must be used
     * @throws IOException if the parsing fails
     */
    public Set<String> getParametersFromFile(String file) throws IOException {
        ConfigurationParser parser = getParser(Utils.getFileType(file));
        return parser.parse(file);
    }

    /**
     * Get the parser for a configuration file based on its file type.
     * @param configFileType the file type of the configuration file
     * @return the parser for the configuration file
     */
    public ConfigurationParser getParser(String configFileType) {
        if (configFileType == null) {
            Log.WARN("Unable to parse configuration file" + configFileType + " from Annotation, use default parser");
            return new NullConfigurationParser();
        }
        switch (configFileType) {
            case "json":
                return new JsonConfigurationParser();
            case "xml":
                return new XmlConfigurationParser();
            default:
                // Default to a parser that returns an empty set
                return new NullConfigurationParser();
        }
    }

    // Internal

    /**
     * Get all the parameters for a test class that every test method in the class must use.
     * @param configTestClass the annotation for the test class
     * @return a set of parameters that every test method in the class must use
     */
    private Set<String> getAllClassParameters(ConfigTestClass configTestClass) throws IOException {
        Set<String> classLevelParameters = new HashSet<>(Arrays.asList(configTestClass.value()));
        String classConfigFile = configTestClass.file();
        if (!classConfigFile.isEmpty()) {
            classLevelParameters.addAll(getParametersFromFile(classConfigFile));
        }
        return classLevelParameters;
    }

    /**
     * Get all the parameters for a test method.
     * @param configTest the annotation for the test method
     * @return a set of parameters that the test method must use
     * @throws IOException if the parsing fails
     */
    private Set<String> getAllMethodParameters(ConfigTest configTest) throws IOException {
        Set<String> methodLevelParameters = new HashSet<>();

        // Retrieve method-level parameters if present
        methodLevelParameters.addAll(Arrays.asList(configTest.value()));
        // Retrieve class-level parameters if present
        methodLevelParameters.addAll(classLevelParameters);

        // Retrieve file-level parameters if present
        String file = configTest.file();
        if (!file.isEmpty()) {
            methodLevelParameters.addAll(getParametersFromFile(file));
        }
        return methodLevelParameters;
    }
}