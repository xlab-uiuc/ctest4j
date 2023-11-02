package edu.illinois;

import edu.illinois.parser.ConfigurationParser;
import edu.illinois.parser.JsonConfigurationParser;
import edu.illinois.parser.NullConfigurationParser;
import edu.illinois.parser.XmlConfigurationParser;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static edu.illinois.Names.USED_CONFIG_FILE_DIR;

/**
 * Author: Shuai Wang
 * Date:  11/1/23
 */
public interface CTestRunner {

    /**
     * Get the parameters from a configuration file.
     * @param file the path of the configuration file
     * @return the set of parameters that must be used
     * @throws IOException if the parsing fails
     */
    default Set<String> getParametersFromFile(String file) throws IOException {
        ConfigurationParser parser = getParser(Utils.getFileType(file));
        return parser.parseConfigNameSet(file);
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
    default Set<String> getAllClassParameters(Set<String> classLevelParameters, String classConfigFile) throws IOException {
        if (!classConfigFile.isEmpty()) {
            classLevelParameters.addAll(getParametersFromFile(classConfigFile));
        }
        return classLevelParameters;
    }

    /**
     * Get all the parameters for a test method.
     * @return a set of parameters that the test method must use
     * @throws IOException if the parsing fails
     */
    default Set<String> getAllMethodParameters(String className, String methodName, String configFile, Set<String> methodLevelParameters, Set<String> classLevelParameters) throws IOException {
        Set<String> allMethodLevelParameters = new HashSet<>();
        // Retrieve method-level parameters if present
        allMethodLevelParameters.addAll(methodLevelParameters);
        // Retrieve class-level parameters if present
        allMethodLevelParameters.addAll(classLevelParameters);

        // Retrieve file-level parameters if present
        if (!configFile.isEmpty()) {
            allMethodLevelParameters.addAll(getParametersFromFile(configFile));
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
        File defaultFile = new File(USED_CONFIG_FILE_DIR, Utils.getTestMethodFullName(className, methodName) + ".json");
        if (defaultFile.exists()) {
            params.addAll(getParametersFromFile(defaultFile.getAbsolutePath()));
        }
        return params;
    }
}
