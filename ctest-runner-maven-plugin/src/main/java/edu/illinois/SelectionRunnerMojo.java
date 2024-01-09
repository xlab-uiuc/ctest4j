package edu.illinois;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Select and run configuration tests based on the given configuration parameters under test.
 * The selection is done on the test class level.
 * Author: Shuai Wang
 * Date:  1/7/24
 */
@Mojo(name = "select",
        requiresDependencyResolution= ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
@Execute(goal = "select", phase = LifecyclePhase.PROCESS_TEST_CLASSES, lifecycle = "select")
public class SelectionRunnerMojo extends AbstractMojo {

    @Component
    protected MavenSession mavenSession;
    @Component
    protected BuildPluginManager pluginManager;

    @Parameter(property="project")
    protected MavenProject project;

    @Parameter(property=Names.CONFIG_PARAMETER_LIST_PROPERTY)
    protected List<String> configParameterList;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        TestClassSelector testClassSelector = new TestClassSelector(new HashSet<>(configParameterList));
        Set<String> selectedTests = testClassSelector.select();
        getLog().debug("Selected test classes: " + selectedTests);

        // If no test class is selected, skip test execution
        if (selectedTests == null || selectedTests.isEmpty()) {
            getLog().info("[CTEST-RUNNER] No test class is selected. Skipping test execution.");
            getLog().info("[CTEST-RUNNER] To run all tests without selection, " +
                    "please directly invoke Maven Surefire with goal \"test\".");
            return;
        }
        runTests(selectedTests);
    }

    /**
     * Launch Maven Surefire to run the given test class.
     * @param testClasses The set of test classes to run
     */
    private void runTests(Set<String> testClasses) throws MojoExecutionException, MojoFailureException {
        SurefireExecution se = new SurefireExecution(lookupPlugin(Names.SUREFIRE_PLUGIN_KEY),
                project, mavenSession, pluginManager, testClasses, getLog());
        se.run();
    }

    /**
     * Find plugin based on the plugin key. Returns null if plugin
     * cannot be located.
     * This is mainly used for running configuration tests with Maven Surefire.
     */
    private Plugin lookupPlugin(String key) {
        List<Plugin> plugins = project.getBuildPlugins();
        for (Plugin plugin : plugins) {
            if (key.equalsIgnoreCase(plugin.getKey())) {
                return plugin;
            }
        }
        return null;
    }
}
