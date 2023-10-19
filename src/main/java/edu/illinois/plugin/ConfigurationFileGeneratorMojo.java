package edu.illinois.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Author: Shuai Wang
 * Date:  10/19/23
 */
@Mojo(name = "generate-config-file", defaultPhase = LifecyclePhase.INITIALIZE)
public class ConfigurationFileGeneratorMojo extends AbstractMojo {
    @Parameter(property = "logFilePath", required = true)
    private String logFilePath;

    @Parameter(property = "outputDir", defaultValue = "config-file-output")
    private String outputDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ConfigFileGenerator generator = new ConfigFileGenerator(logFilePath, outputDir);
            generator.generateConfigFile();
        } catch (Exception e) {
            getLog().error(e);
            throw new RuntimeException(e);
        }
    }
}
