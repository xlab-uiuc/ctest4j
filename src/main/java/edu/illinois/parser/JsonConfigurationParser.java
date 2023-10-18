package edu.illinois.parser;

/**
 * Author: Shuai Wang
 * Date:  10/17/23
 */
import javax.json.*;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashSet;
import java.util.Set;

public class JsonConfigurationParser implements ConfigurationParser {

    /**
     * Parse the JSON configuration file and return the set of parameters that must be used in each test method.
     * @param jsonFilePath the path of the JSON configuration file
     * @return the set of parameters that must be used
     * @throws IOException if the parsing fails
     */
    @Override
    public Set<String> parse(String jsonFilePath) throws IOException {
        FileReader fileReader = new FileReader(jsonFilePath);
        JsonReader jsonReader = Json.createReader(fileReader);
        JsonObject jsonObject = jsonReader.readObject();

        jsonReader.close();
        fileReader.close();

        Set<String> mustSet = new HashSet<>();
        // Get the "must" field
        if (jsonObject.containsKey(MUST_FIELD)) {
            for (JsonValue must : jsonObject.getJsonArray(MUST_FIELD)) {
                mustSet.add(((JsonString) must).getString());
            }
        }
        return mustSet;
    }
}
