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
     * Resolve the mapping file path. If the mapping file path is empty, try the default mapping file path.
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
     * Get the parameters from a configuration file.
     * @param file the path of the configuration file
     * @return the set of parameters that must be used
     * @throws IOException if the parsing fails
     */
    default Set<String> getParametersFromMappingFile(String file) throws IOException {
        ConfigurationParser parser = getParser(Utils.getFileType(file));
        return parser.parseConfigNameSet(file);
    }

    /**
     * Get the class-level parameters from a configuration mapping file.
     */
    default Set<String> getClasssParametersFromMappingFile(String configMappingFile) throws IOException {
        ConfigurationParser parser = getParser(Utils.getFileType(configMappingFile));
        return parser.getClassLevelRequiredConfigParam(configMappingFile);
    }

    default Map<String, Set<String>> getAllMethodLevelParametersFromMappingFile(String configMappingFile) throws IOException {
        ConfigurationParser parser = getParser(Utils.getFileType(configMappingFile));
        return parser.getMethodLevelRequiredConfigParam(configMappingFile);
    }

    /**
     * Get the method-level parameters from a configuration mapping file.
     */
    default Set<String> getMethodParametersFromMappingFile(String configMappingFile, String methodName) throws IOException {
        ConfigurationParser parser = getParser(Utils.getFileType(configMappingFile));
        Map<String, Set<String>> methodLevelRequiredConfigParam = parser.getMethodLevelRequiredConfigParam(configMappingFile);
        if (methodLevelRequiredConfigParam.containsKey(methodName)) {
            return methodLevelRequiredConfigParam.get(methodName);
        } else {
            return new HashSet<>();
        }
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
     * Search whether there is a default place that specify the file
     * @return the set of parameters that must be used
     * @throws IOException if the parsing fails
     */
    default Set<String> getRequiredParametersFromDefaultFile(String className, String methodName) throws IOException {
        Set<String> params = new HashSet<>();
        File defaultFile = new File(CONFIG_MAPPING_DIR, Utils.getTestMethodFullName(className, methodName) + ".json");
        if (defaultFile.exists()) {
            params.addAll(getParametersFromMappingFile(defaultFile.getAbsolutePath()));
        }
        return params;
    }

/*
    default void checkCTestParameterUsage(Set<String> params) throws UnUsedConfigParamException {
        if (Options.mode == Modes.CHECKING || Options.mode == Modes.DEFAULT) {
            for (String param : params) {
                if (!ConfigTracker.isParameterUsed(param)) {
                    throw new UnUsedConfigParamException(param + " was not used during the test.");
                }
            }
        }
    }
*/

    default void writeConfigUsageToJson(ConfigUsage configUsage, File targetFile) {
        if (saveUsedParamToFile) {
            ConfigUsage.updateAllConfigUsage(configUsage);
            ConfigUsage.writeToJson(configUsage, targetFile);
        }
    }

    /**
     * Check whether the exception is an UnUsedConfigParamException.
     */
    default boolean isUnUsedParamException(Class<? extends Throwable> expected) {
        return expected.isAssignableFrom(UnUsedConfigParamException.class);
    }

    /**
     * Swallow the exception thrown from test method if the exception is an Assertion Error that expects UnUsedConfigParamException.
     */
    default boolean shouldThorwException(Throwable throwable) {
        return throwable != null && !throwable.getMessage().equals("Expected exception: edu.illinois.UnUsedConfigParamException");
    }

    default boolean isCurrentTestIgnored(Set<String> targetParams, Set<String> usedParams) {
        if (Names.CTEST_RUNTIME_SELECTION && !targetParams.isEmpty()) {
            // return true if none of the parameters in targetParams is used
            return Collections.disjoint(targetParams, usedParams);
        }
        return false;
    }
}
