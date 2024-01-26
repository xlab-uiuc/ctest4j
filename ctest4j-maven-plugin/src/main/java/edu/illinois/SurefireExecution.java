package edu.illinois;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.util.HashSet;
import java.util.Set;

public class SurefireExecution {
    /** Names for Maven Surefire Configuration Elements */
    private final String MAVEN_SUREFIRE_INCLUDES = "includes";
    private final String MAVEN_SUREFIRE_INCLUDE = "include";
    private final String MAVEN_SUREFIRE_EXCLUDES = "excludes";
    private final String MAVEN_SUREFIRE_TEST_GOAL = "test";
    private final String MAVEN_FORKCOUNT = "forkCount";

    /** Maven Project objects */
    private final Plugin surefire;
    private final MavenProject mavenProject;
    private final MavenSession mavenSession;
    private final BuildPluginManager pluginManager;

    /** The set of test classes to run with Surefire */
    private final Set<String> testClasses;

    /** Maven log */
    private final Log log;

    public SurefireExecution(Plugin surefire, MavenProject mavenProject, MavenSession mavenSession,
                             BuildPluginManager pluginManager, Set<String> testClasses, Log log) {
        this.surefire = surefire;
        this.mavenProject = mavenProject;
        this.mavenSession = mavenSession;
        this.pluginManager = pluginManager;
        this.testClasses = new HashSet<>(testClasses);
        this.log = log;
    }

    /**
     * Invoke Maven Surefire to run @testClassName
     */
    public void run() throws MojoExecutionException, MojoFailureException {
        try {
            Xpp3Dom configuration = modifySurefireConfiguration((Xpp3Dom) this.surefire.getConfiguration());
            MojoExecutor.executeMojo(this.surefire, MojoExecutor.goal(MAVEN_SUREFIRE_TEST_GOAL), configuration,
                    MojoExecutor.executionEnvironment(this.mavenProject, this.mavenSession, this.pluginManager));
        } catch (MojoExecutionException e) {
            if (e.getCause() instanceof MojoFailureException) {
                throw new MojoFailureException("Maven Surefire Failure: ", e.getCause());
            }
            throw new MojoExecutionException("Unable to execute maven surefire" + e.getCause());
        }
    }

    /**
     * Modify the Maven Surefire Configuration to only include the selected test classes
     * @param conf The original Maven Surefire Configuration
     * @return Modified Maven Surefire Configuration Object
     */
    private Xpp3Dom modifySurefireConfiguration(Xpp3Dom conf) {
        if(conf == null) {
            conf = new Xpp3Dom("configuration");
        }
        Xpp3Dom retConf = new Xpp3Dom(conf);
        removeChild(retConf, MAVEN_SUREFIRE_INCLUDES);
        removeChild(retConf, MAVEN_SUREFIRE_EXCLUDES);
        addSurefireIncludes(retConf);
        //modifyMavenForkCountToZero(retConf); --> Remove this and let user to manually set forkCount in the buggy env.
        return retConf;
    }

    // Internal

    /** For some projects that will fail surefire:test when fork count
     * is larger than 0. (e.g., ZooKeeper)
     */
    private void modifyMavenForkCountToZero(Xpp3Dom node) {
        int nodeIndex = getChildIndex(node, MAVEN_FORKCOUNT);
        if (nodeIndex >= 0) {
            node.removeChild(nodeIndex);
        }
        Xpp3Dom modifiedForkCount = makeNode(MAVEN_FORKCOUNT, "0");
        node.addChild(modifiedForkCount);
    }

    private void removeChild(Xpp3Dom node, String name) {
        int index = getChildIndex(node, name);
        if (index >= 0) {
            this.log.debug("remove " + name + " now");
            node.removeChild(index);
        }
    }

    private int getChildIndex(Xpp3Dom node, String childName) {
        for(int i = 0; i < node.getChildCount(); i++) {
            if (node.getChild(i).getName().equalsIgnoreCase(childName)) {
                this.log.debug(childName + " at index: " + i);
                return i;
            }
        }
        return -1;
    }

    private void addSurefireIncludes(Xpp3Dom node) {
        Xpp3Dom includes = makeNode(MAVEN_SUREFIRE_INCLUDES);
        for (String testClassName : this.testClasses) {
            includes.addChild(makeNode(MAVEN_SUREFIRE_INCLUDE, "**/" + testClassName));
        }
        this.log.debug("adding includes " + includes);
        node.addChild(includes);
    }

    private Xpp3Dom makeNode(String name) {
        Xpp3Dom node = new Xpp3Dom(name);
        return node;
    }

    private Xpp3Dom makeNode(String name, String value) {
        Xpp3Dom node = new Xpp3Dom(name);
        node.setValue(value);
        return node;
    }
}
