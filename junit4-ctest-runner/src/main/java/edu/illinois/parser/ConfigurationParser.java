package edu.illinois.parser;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  10/17/23
 */
public interface ConfigurationParser {
    public static String REQUIRED = "required";

    /**
     * Parse the configuration file and return the set of parameters that must be used in each test method.
     * @param configFilePath the path of the configuration file
     * @return the set of parameters that must be used
     * @throws IOException
     */
    Set<String> parseConfigNameSet(String configFilePath) throws IOException;

    Map<String, String> parseConfigNameValueMap(String configFilePath) throws IOException;
}

