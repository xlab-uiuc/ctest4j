package edu.illinois.plugin;

import edu.illinois.parser.ConfigurationParser;

import java.io.*;
import java.util.*;
import javax.json.*;

import static edu.illinois.Names.TRACKING_LOG_PREFIX;

/**
 * Generate config file for each test method
 * Author: Shuai Wang
 * Date:  10/19/23
 */
public class ConfigFileGenerator {
    private final File logFile;
    private final File outputDir;
    private Map<String, Set<String>> methodToUsedParams;

    public ConfigFileGenerator(String filePath, String outputDirPath) {
        this.logFile = new File(filePath);
        this.outputDir = new File(outputDirPath);
        if (!logFile.exists() || !logFile.isFile()) {
            throw new RuntimeException("File " + filePath + " does not exist");
        }
        if (!outputDir.exists() || !outputDir.isDirectory()) {
            throw new RuntimeException("Directory " + outputDir + " does not exist");
        }
        this.methodToUsedParams = new HashMap<>();
    }

    /**
     * Generate config file for each test method
     * @throws IOException if the log file does not exist
     */
    public void generateConfigFile() throws IOException {
        readLogFile();
        for (String methodName : methodToUsedParams.keySet()) {
            Set<String> usedParams = methodToUsedParams.get(methodName);
            String configFileName = methodName + ".json";
            File configFile = new File(outputDir, configFileName);
            if (!configFile.exists()) {
                if (!configFile.createNewFile()) {
                    throw new RuntimeException("Failed to create config file " + configFileName);
                }
            }
            writeParamSetToJson(usedParams, configFile);
        }
    }

    /**
     * Write the set of parameters to a JSON file
     * @param paramSet the set of parameters
     * @param jsonFile the JSON file
     */
    private void writeParamSetToJson(Set<String> paramSet, File jsonFile) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder requiredArrayBuilder = Json.createArrayBuilder();

        // Add parameters to the "required" array
        for (String param : paramSet) {
            requiredArrayBuilder.add(param);
        }
        jsonObjectBuilder.add(ConfigurationParser.REQUIRED, requiredArrayBuilder);

        // Write the JsonObject to a JSON file
        JsonObject jsonObject = jsonObjectBuilder.build();
        try (FileWriter fileWriter = new FileWriter(jsonFile)) {
            JsonWriter jsonWriter = Json.createWriter(fileWriter);
            jsonWriter.writeObject(jsonObject);
            jsonWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON file " + jsonFile);
        }
    }

    /**
     * Read the log file and get the set of parameters used in each test method
     * @throws IOException if the log file does not exist
     */
    private void readLogFile() throws IOException {
        String line;
        // read each line of logFile, and check whether the line contains TRACKING_LOG_PREFIX
        BufferedReader reader = new BufferedReader(new FileReader(logFile));
        while ((line = reader.readLine()) != null) {
            // Process each line here
            if (line.contains(TRACKING_LOG_PREFIX)) {
                String methodName = getMethodName(line);
                Set<String> usedParams = getUsedParams(line);
                methodToUsedParams.put(methodName, usedParams);
            }
        }
    }

    /**
     * Get the set of parameters used in a test method
     * @param line the line of log file
     * @return the set of parameters used in the test method
     */
    private Set<String> getUsedParams(String line) {
        String[] parts = line.split(" ");
        String params = parts[parts.length - 1];
        params = params.substring(1, params.length() - 1);
        String[] paramArray = params.split(",");
        Set<String> paramSet = new HashSet<>();
        for (String param : paramArray) {
            paramSet.add(param.trim());
        }
        return paramSet;
    }

    /**
     * Get the method name from a line of log file
     * @param line the line of log file
     * @return the method name
     */
    private String getMethodName(String line) {
        // Split by TRACKING_LOG_PREFIX and the method name is right after it
        String[] parts = line.split(" ");
        return parts[8];
    }

}
