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
public class XmlConfigurationParser implements ConfigurationParser {
    //TODO: implement this class
    @Override
    public Set<String> parseConfigNameSet(String xmlFilePath) throws IOException {
        return new HashSet<>();
    }

    @Override
    public Map<String, String> parseConfigNameValueMap(String configFilePath) throws IOException {
        return new HashMap<>();
    }
}
