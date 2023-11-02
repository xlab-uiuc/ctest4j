package edu.illinois.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  10/17/23
 */
public class NullConfigurationParser implements ConfigurationParser {

    /**
     * Default configuration parser that does not parse anything and return an empty set.
     * @param configFilePath the path of the configuration file
     * @return an empty set
     */
    @Override
    public Set<String> parseConfigNameSet(String configFilePath)  {
        return new HashSet<>();
    }

    @Override
    public Map<String, String> parseConfigNameValueMap(String configFilePath) throws IOException {
        return new HashMap<>();
    }


}
