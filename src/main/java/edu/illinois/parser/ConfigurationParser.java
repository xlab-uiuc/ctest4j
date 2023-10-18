package edu.illinois.parser;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  10/17/23
 */
public interface ConfigurationParser {
    public static String MUST_FIELD = "must";

    /**
     * Parse the configuration file and return the set of parameters that must be used in each test method.
     * @param configFilePath the path of the configuration file
     * @return the set of parameters that must be used
     * @throws IOException
     */
    Set<String> parse(String configFilePath) throws IOException;
}
