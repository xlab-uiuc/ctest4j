package edu.illinois;

import edu.illinois.parser.ConfigurationParser;
import edu.illinois.parser.JsonConfigurationParser;
import edu.illinois.parser.NullConfigurationParser;
import edu.illinois.parser.XmlConfigurationParser;

import java.io.File;
import java.io.IOException;
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
     * @param kclass the test class
     * @throws Exception if the initialization fails
     */
    void initializeRunner(Class<?> kclass) throws Exception;

    /**
     * Check configuration parameter usage after each test method is executed.
     * @param method
     * @throws Exception if the check fails
     */

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
    default Set<String> getUnionClassParameters(Set<String> classLevelParameters, String classConfigFile) throws IOException {
        if (!classConfigFile.isEmpty()) {
            classLevelParameters.addAll(getParametersFromMappingFile(classConfigFile));
        }
        return classLevelParameters;
    }

    /**
     * Get all the parameters for a test method.
     * @return a set of parameters that the test method must use
     * @throws IOException if the parsing fails
     */
    default Set<String> getUnionMethodParameters(String className, String methodName, String methodLevelConfigMappingFile,
                                                 Set<String> methodLevelParameters, Set<String> classLevelParameters) throws IOException {
        Set<String> allMethodLevelParameters = new HashSet<>();
        // Retrieve method-level parameters if present
        allMethodLevelParameters.addAll(methodLevelParameters);
        // Retrieve class-level parameters if present
        allMethodLevelParameters.addAll(classLevelParameters);

        // Retrieve file-level parameters if present
        if (!methodLevelConfigMappingFile.isEmpty()) {
            allMethodLevelParameters.addAll(getParametersFromMappingFile(methodLevelConfigMappingFile));
        } else {
            allMethodLevelParameters.addAll(getRequiredParametersFromDefaultFile(className, methodName));
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

    default void checkCTestParameterUsage(Set<String> params) throws UnUsedConfigParamException {
        if (Options.mode == Modes.CHECKING || Options.mode == Modes.DEFAULT) {
            for (String param : params) {
                if (!ConfigTracker.isParameterUsed(param)) {
                    throw new UnUsedConfigParamException(param + " was not used during the test.");
                }
            }
        }
    }

    default void writeConfigToFile(String fileName) {
        if (saveUsedParamToFile) {
            ConfigTracker.writeConfigToFile(fileName);
        }
    }
}
