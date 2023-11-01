package edu.illinois.junit4;

import edu.illinois.*;
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

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static edu.illinois.Names.USED_CONFIG_FILE_DIR;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class CTestRunner extends BlockJUnit4ClassRunner {
    private final Set<String> classLevelParameters;

    public CTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        // Retrieve class-level parameters if present
        CTestClass cTestClassAnnotation = klass.getAnnotation(CTestClass.class);
        if (cTestClassAnnotation != null) {
            try {
                classLevelParameters = getAllClassParameters(cTestClassAnnotation);
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse configuration file from class " + klass.getName() + " Annotation", e);
            }
        } else {
            classLevelParameters = new HashSet<>();
        }
        ConfigTracker.setCurrentTestClassName(klass.getName());
    }

    /**
     * Get all the test methods with @Test and @CTest annotations.
     * @return a list of test methods.
     */
    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        // Return both methods with @Test and @CTest annotations
        List<FrameworkMethod> methods = new ArrayList<>();
        methods.addAll(super.computeTestMethods());
        methods.addAll(getTestClass().getAnnotatedMethods(CTest.class));
        return Collections.unmodifiableList(methods);
    }

    /**
     * Invoke the configuration tests with @CTest annotation.
     * @param method
     * @param test
     * @return ConfigTestStatement if the method has @CTest annotation,
     * otherwise return the original statement.
     */
    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement base = super.methodInvoker(method, test);
        if (Options.mode == Modes.BASE) {
            return base;
        }
        CTest cTest = method.getAnnotation(CTest.class);
        if (cTest != null) {
            try {
                return new CTestStatement(base, getAllMethodParameters(cTest, method));
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

    protected Statement vanillaMethodInvoker(FrameworkMethod method, Object test) {
        return super.methodInvoker(method, test);
    }

    /**
     * @CTest would override the expected exception from @Test annotation.
     */
    @Override
    protected Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
        // From @CTest annotation
        CTest cTest = method.getAnnotation(CTest.class);
        if (cTest != null) {
            Class<? extends Throwable> configTestExpectedException = cTest.expected();
            if (configTestExpectedException != CTest.None.class) {
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
     * @CTest would override the timeout from @Test annotation.
     */
    @Override
    protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
        long timeout = 0;
        CTest cTest = method.getAnnotation(CTest.class);
        if (cTest != null) {
            timeout = cTest.timeout();
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
        return parser.parseConfigNameSet(file);
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
     * @param cTestClass the annotation for the test class
     * @return a set of parameters that every test method in the class must use
     */
    private Set<String> getAllClassParameters(CTestClass cTestClass) throws IOException {
        Set<String> classLevelParameters = new HashSet<>(Arrays.asList(cTestClass.value()));
        String classConfigFile = cTestClass.file();
        if (!classConfigFile.isEmpty()) {
            classLevelParameters.addAll(getParametersFromFile(classConfigFile));
        }
        return classLevelParameters;
    }

    /**
     * Get all the parameters for a test method.
     * @param cTest the annotation for the test method
     * @return a set of parameters that the test method must use
     * @throws IOException if the parsing fails
     */
    private Set<String> getAllMethodParameters(CTest cTest, FrameworkMethod method) throws IOException {
        Set<String> methodLevelParameters = new HashSet<>();

        // Retrieve method-level parameters if present
        methodLevelParameters.addAll(Arrays.asList(cTest.value()));
        // Retrieve class-level parameters if present
        methodLevelParameters.addAll(classLevelParameters);

        // Retrieve file-level parameters if present
        String file = cTest.file();
        if (!file.isEmpty()) {
            methodLevelParameters.addAll(getParametersFromFile(file));
        } else {
            methodLevelParameters.addAll(getRequiredParametersFromDefaultFile(method));
        }
        return methodLevelParameters;
    }

    /**
     * Search whether there is a default place that specify the file
     * @param method the test method
     * @return the set of parameters that must be used
     * @throws IOException if the parsing fails
     */
    protected Set<String> getRequiredParametersFromDefaultFile(FrameworkMethod method) throws IOException {
        Set<String> params = new HashSet<>();
        File defaultFile = new File(USED_CONFIG_FILE_DIR, Utils.getTestMethodFullName(method) + ".json");
        if (defaultFile.exists()) {
            params.addAll(getParametersFromFile(defaultFile.getAbsolutePath()));
        }
        return params;
    }
}