package de.schablinski.gradle.activejdbc

import groovy.util.logging.Log4j2
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

@Log4j2
abstract class AbstractActiveJDBCGradlePluginTest extends Specification {

    private static final String TEST_PROPS_FILE = "test.properties"
    private static final Properties TEST_PROPS = new Properties()

    static
    {
        InputStream testPropsStream = ActiveJDBCGradlePluginScalaTest.class.getResourceAsStream("/" + TEST_PROPS_FILE)
        TEST_PROPS.load(testPropsStream)
        testPropsStream.close()
    }


    @TempDir
    File testProjectDir

    List pluginClasspath

    File buildFile
    File gradleSettings

    def setup() {
        pluginClasspath = getClass().classLoader.findResource('plugin-classpath.txt').readLines().collect { new File(it) }

        buildFile = new File(testProjectDir, 'build.gradle')
        gradleSettings = new File(testProjectDir, 'settings.gradle')
        gradleSettings.write('rootProject.name = \'activejdbc-gradle-plugin-test-project\'')
        log.debug "buildFile=$buildFile"
    }

    protected GradleRunner createGradleRunner(String taskName) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath(pluginClasspath)
                .withArguments(taskName, '--info', '--stacktrace')
        // .withDebug(true)
    }
}
