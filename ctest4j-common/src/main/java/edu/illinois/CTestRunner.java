package edu.illinois;

import edu.illinois.parser.ConfigurationParser;
import edu.illinois.parser.JsonConfigurationParser;
import edu.illinois.parser.NullConfigurationParser;
import edu.illinois.parser.XmlConfigurationParser;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.AnnotationFormatError;
import java.util.*;

import static edu.illinois.Names.CONFIG_MAPPING_DIR;
import static edu.illinois.Options.saveUsedParamToFile;

/**
 * Author: Shuai Wang
 * Date:  11/1/23
 */
public interface CTestRunner {

    /**
     * Initialize the runner.
     * @param context the context to initialize the runner
     */
    void initializeRunner(Object context) throws AnnotationFormatError, IOException;

    /**
     * This method must be called right before the test class execution.
     */
    default void startTestClass(String testClassName) {
        Utils.setCurTestClassNameToPTid(Utils.getPTid(), testClassName);
    }

    /**
     * This method must be called right before each of the test method execution.
     */
    default void startTestMethod(String testClassName, String testMethodName) {
        Utils.setCurTestFullNameToPTid(Utils.getPTid(), testClassName, testMethodName);
        ConfigTracker.startTestMethod(testClassName, testMethodName);
    }

    default void endTestMethod(ConfigUsage configUsage, String testClassName, String testMethodName,
                               CTest cTest, Object testAnnotation,
                               Set<String> classLevelParameters,
                               Map<String, Set<String>> methodLevelParametersFromMappingFile) {
        if (Options.mode == Modes.BASE) {
            return;
        }
        ConfigUsage.bufferForUpdate(configUsage, testClassName, testMethodName);
        if (cTest != null) {
            checkConfigurationParameterUsage(testClassName, testMethodName, cTest.regex(), cTest.value(),
                    cTest.expected(), classLevelParameters, methodLevelParametersFromMappingFile);
        } else if (testAnnotation != null) {
            Class<? extends Throwable> annotationExpected = null;
            if (testAnnotation instanceof org.junit.Test) {
                annotationExpected = ((org.junit.Test) testAnnotation).expected();
            }
            checkConfigurationParameterUsage(testClassName, testMethodName, "", new String[]{},
                    annotationExpected, classLevelParameters, methodLevelParametersFromMappingFile);
        }
    }

    default void checkConfigurationParameterUsage(String testClassName, String methodName,
                                                  String annotationRegex, String[] annotationValue,
                                                  Class<? extends Throwable> annotationExpected,
                                                  Set<String> classLevelParameters,
                                                  Map<String, Set<String>> methodLevelParametersFromMappingFile) throws UnUsedConfigParamException {
        if (Options.mode == Modes.CHECKING || Options.mode == Modes.DEFAULT) {
            for (String param : getUnionMethodParameters(testClassName, methodName, annotationRegex,
                            classLevelParameters, methodLevelParametersFromMappingFile,
                            new HashSet<>(Arrays.asList(annotationValue)))) {
                if (!ConfigTracker.isParameterUsed(testClassName, methodName, param)) {
                    if (isUnUsedParamException(annotationExpected)) {
                        return;
                    }
                    throw new UnUsedConfigParamException(param + " was not used during the test.");
                }
            }
        }
    }

    /**
     * Initialize the class-level and method-level parameters from the mapping file.
     */
    default Object[] initializeParameterSet(String testClassName, String mappingFilePath, String[] annotationValue, String annotationRegex) throws IOException {
        mappingFilePath = resolveMappingFilePath(mappingFilePath, testClassName);
        if (mappingFilePath.isEmpty()) {
            return new Object[]{getValueAndRegexClassParameters(new HashSet<>(Arrays.asList(annotationValue)), annotationRegex), new HashMap<>()};
        }

        File configFile = new File(mappingFilePath);
        if (!configFile.exists()) {
            throw new IOException("The configuration mapping file " + mappingFilePath + " does not exist.");
        }

        Set<String> classParams = getUnionClassParameters(new HashSet<>(Arrays.asList(annotationValue)), mappingFilePath, annotationRegex);
        Map<String, Set<String>> methodParams = getAllMethodLevelParametersFromMappingFile(mappingFilePath);

        return new Object[]{classParams, methodParams};
    }

    /**
     * Resolve the mapping file path.
     * If the mapping file path is empty, try the default mapping file path under the @CONFIG_MAPPING_DIR.
     */
    private String resolveMappingFilePath(String mappingFile, String testClassName) {
        if (mappingFile.isEmpty()) {
            File defaultMappingFile = new File(CONFIG_MAPPING_DIR, testClassName + ".json");
            return defaultMappingFile.exists() ? defaultMappingFile.getAbsolutePath() : mappingFile;
        }
        return mappingFile;
    }

    /**
     * Get the configuration parameter name set from the given regex.
     */
    default Set<String> getParametersFromRegex(String regex) {
        Set<String> params = new HashSet<>();
        if (!regex.isEmpty()) {
            params.addAll(new ConfigRegex(regex).getParameters());
        }
        return params;
    }

    /**
     * Get the configuration parameter name set from a configuration file.
     */
    default Set<String> getParametersFromMappingFile(String file) throws IOException {
        ConfigurationParser parser = getParser(Utils.getFileType(file));
        return parser.parseConfigNameSet(file);
    }

    /**
     * Get the class-level parameters from a configuration mapping file.
     */
    default Set<String> getClassParametersFromMappingFile(String configMappingFile) throws IOException {
        ConfigurationParser parser = getParser(Utils.getFileType(configMappingFile));
        return parser.getClassLevelRequiredConfigParam(configMappingFile);
    }

    /**
     * Get the method-level parameters from a configuration mapping file.
     */
    default Map<String, Set<String>> getAllMethodLevelParametersFromMappingFile(String configMappingFile) throws IOException {
        ConfigurationParser parser = getParser(Utils.getFileType(configMappingFile));
        return parser.getMethodLevelRequiredConfigParam(configMappingFile);
    }


    /**
     * Get the parser for a configuration file based on its file type.
     * @param configFileType the file type of the configuration file
     * @return the parser for the configuration file
     */
    default ConfigurationParser getParser(String configFileType) {
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

    /**
     * Ctest configuration parameter usage checking:
     * Get all the parameters for a test class that every test method in the class must use.
     * @return a set of parameters that every test method in the class must use
     */
    default Set<String> getUnionClassParameters(Set<String> classLevelParameters, String classConfigFile, String classRegex) throws IOException {
        Set<String> result = new HashSet<>(getValueAndRegexClassParameters(classLevelParameters, classRegex));
        if (!classConfigFile.isEmpty()) {
            result.addAll(getParametersFromMappingFile(classConfigFile));
        }
        return result;
    }

    /**
     * Ctest configuration parameter usage checking:
     * Get the parameters from the regex and the parameters from the class-level annotation.
     */
    default Set<String> getValueAndRegexClassParameters(Set<String> classLevelParameters, String classRegex) throws IOException {
        Set<String> result = new HashSet<>(classLevelParameters);
        if (!classRegex.isEmpty()) {
            result.addAll(getParametersFromRegex(classRegex));
        }
        return result;
    }

    /**
     * Ctest configuration parameter usage checking:
     * Get all the parameters for a test method.
     * @return a set of parameters that the test method must use
     * @throws IOException if the parsing fails
     */
    default Set<String> getUnionMethodParameters(String testClassName, String methodName, String methodRegex,
                                                 Set<String> classLevelParameters, Map<String, Set<String>> methodLevelParametersFromMappingFile,
                                                 Set<String> methodLevelParamsFromAnnotation) {
        methodName = Utils.getFullTestName(testClassName, methodName);
        Set<String> allMethodLevelParameters = new HashSet<>();
        // Retrieve class-level parameters if present
        allMethodLevelParameters.addAll(classLevelParameters);
        // Retrieve method-level parameters if present
        Set<String> methodLevelParameters = methodLevelParametersFromMappingFile.get(methodName);
        if (methodLevelParameters != null) {
            allMethodLevelParameters.addAll(methodLevelParameters);
        }
        allMethodLevelParameters.addAll(methodLevelParamsFromAnnotation);

        // Retrieve regex-level parameters if present
        if (!methodRegex.isEmpty()) {
            allMethodLevelParameters.addAll(getParametersFromRegex(methodRegex));
        }
        return allMethodLevelParameters;
    }

    /**
     * Ctest configuration mapping file creation:
     * Write the used parameters to a JSON file.
     */
    default void writeConfigUsageToJson(ConfigUsage configUsage, File targetFile) {
        if (saveUsedParamToFile) {
            ConfigUsage.updateAllConfigUsage(configUsage);
            ConfigUsage.writeToJson(configUsage, targetFile);
        }
    }

    /**
     * Ctest configuration parameter usage checking:
     * Check whether the exception is an UnUsedConfigParamException.
     */
    default boolean isUnUsedParamException(Class<? extends Throwable> expected) {
        if (expected == null) {
            return false;
        }
        return expected.isAssignableFrom(UnUsedConfigParamException.class);
    }

    /**
     * Ctest configuration parameter usage checking:
     * Swallow the exception thrown from test method if the exception is an Assertion Error that expects UnUsedConfigParamException.
     */
    default boolean shouldThrowException(Throwable throwable) {
        return throwable != null && !throwable.getMessage().equals("Expected exception: edu.illinois.UnUsedConfigParamException");
    }

    /**
     * Ctest selection:
     * Check whether the current test is ignored.
     */
    default boolean isCurrentTestIgnored(Set<String> targetParams, Set<String> usedParams) {
        if (Names.CTEST_RUNTIME_SELECTION && !targetParams.isEmpty()) {
            // return true if none of the parameters in targetParams is used
            return Collections.disjoint(targetParams, usedParams);
        }
        return false;
    }
}
