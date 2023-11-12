package edu.illinois;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static edu.illinois.ConfigUsage.fromJson;
import static edu.illinois.ConfigUsage.writeToJson;

/**
 * Author: Shuai Wang
 * Date:  11/8/23
 */
public class ConfigUsageTest {
    @Test
    public void test() throws IOException {
        ConfigUsage config = new ConfigUsage();

        config.addClassLevelParams(new HashSet<>() {{
            add("param1");
            add("param2");
        }});

        Set<String> methodParams = new HashSet<>();
        methodParams.add("methodParam1");
        methodParams.add("methodParam2");
        config.addMethodLevelParams("test", methodParams);

        // Serialize the ConfigUsage object to JSON

        writeToJson(config, new File("config.json"));
        ConfigUsage deserializedConfig = fromJson(Utils.readStringFromFile("config.json"));

        // Use the deserialized object
        System.out.println("Deserialized object: ");
        System.out.println("Class level params: " + deserializedConfig.getClassLevelParams());
        System.out.println("Method level params: " + deserializedConfig.getMethodLevelParams());
    }

    @After
    public void cleanUp() {
        Utils.deleteFile("config.json");
    }
}
