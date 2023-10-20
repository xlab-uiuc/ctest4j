package edu.illinois.parser;

/**
 * Author: Shuai Wang
 * Date:  10/17/23
 */
import javax.json.*;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JsonConfigurationParser implements ConfigurationParser {

    /**
     * Parse the JSON configuration file and return the set of parameters that must be used in each test method.
     * @param jsonFilePath the path of the JSON configuration file
     * @return the set of parameters that must be used
     * @throws IOException if the parsing fails
     */
    @Override
    public Set<String> parseConfigNameSet(String jsonFilePath) throws IOException {
        FileReader fileReader = new FileReader(jsonFilePath);
        JsonReader jsonReader = Json.createReader(fileReader);
        JsonObject jsonObject = jsonReader.readObject();

        jsonReader.close();
        fileReader.close();

        Set<String> requiredSet = new HashSet<>();
        // Get the "required" field
        if (jsonObject.containsKey(REQUIRED)) {
            for (JsonValue required : jsonObject.getJsonArray(REQUIRED)) {
                requiredSet.add(((JsonString) required).getString());
            }
        }
        return requiredSet;
    }

    /**
     * Parse the JSON configuration file and return the map of parameters and their values.
     * @param configFilePath the path of the JSON configuration file
     * @return the map of parameters and their values
     * @throws IOException if the parsing fails
     */
    @Override
    public Map<String, String> parseConfigNameValueMap(String configFilePath) throws IOException {
        FileReader fileReader = new FileReader(configFilePath);
        JsonReader jsonReader = Json.createReader(fileReader);
        JsonObject jsonObject = jsonReader.readObject();

        jsonReader.close();
        fileReader.close();

        Map<String, String> configNameValueMap = new HashMap<>();
        // Iterate over the JSON object and put the key-value pairs into the map
        for (Map.Entry<String, JsonValue> entry : jsonObject.entrySet()) {
            configNameValueMap.put(entry.getKey(), ((JsonString)entry.getValue()).getString());
        }
        return configNameValueMap;
    }
}
