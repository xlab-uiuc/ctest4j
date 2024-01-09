package edu.illinois.select;

import edu.illinois.Names;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: Shuai Wang
 * Date:  1/7/24
 */
public class TestSelector {
    /** The set of configuration parameters to be tested. */
    protected final Set<String> targetParams;
    protected final File mappingDir = new File(Names.CONFIG_MAPPING_DIR);

    public TestSelector(Set<String> params) {
        targetParams = new HashSet<>(params);
    }

    public TestSelector() {
        this(new HashSet<>());
    }

    public void addTargetParam(String param) {
        targetParams.add(param);
    }

    public void setTargetParams(Set<String> params) {
        targetParams.clear();
        targetParams.addAll(params);
    }

    public void clearTargetParams() {
        targetParams.clear();
    }

    public Set<String> getTargetParams() {
        return Collections.unmodifiableSet(targetParams);
    }

    public boolean hasMappingDir() {
        return mappingDir.exists() && mappingDir.isDirectory();
    }

    /**
     * * Select the test classes to be run based on the mapping files.
     * @return the set of test classes to be selected, or null if no mapping dir
     * @throws RuntimeException if any error occurs
     */
    public Set<String> select() {
        if (hasMappingDir()) {
            // Select test class directly based on the mapping files
            try {
                Set<String> matchingFiles = Files.walk(mappingDir.toPath())
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .filter(path -> testUsedTargetParams(path, targetParams))
                        .parallel() // Use parallel processing
                        .map(this::getTestClassNameFromPath)
                        .collect(Collectors.toSet());

                // Print or use the matching file names
                matchingFiles.forEach(System.out::println);
                return matchingFiles;
            } catch (Exception e) {
                throw new RuntimeException("Error while selecting tests from mapping dir "
                        + mappingDir.getAbsolutePath(), e);
            }
        }

        // If no mapping dir, return null and do runtime selection
        return null;
    }

    /**
     * If any of the target parameters is used in the file, return true
     * @return true if any of the target parameters is used in the file
     */
    private boolean testUsedTargetParams(Path filePath, Set<String> targetParams) {
        try {
            String content = Files.readString(filePath);
            return targetParams.stream().anyMatch(content::contains);
        } catch (Exception e) {
            throw new RuntimeException("Error while reading file " + filePath.toString(), e);
        }
    }

    private String getTestClassNameFromPath(Path filePath) {
        String fileName = filePath.getFileName().toString();
        return fileName.substring(0, fileName.indexOf(".")); // Remove the extension
    }
}
