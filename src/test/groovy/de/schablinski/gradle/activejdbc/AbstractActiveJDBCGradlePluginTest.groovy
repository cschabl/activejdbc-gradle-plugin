package de.schablinski.gradle.activejdbc

import groovy.util.logging.Log4j2
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

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

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder(new File(TEST_PROPS.getProperty("gradleTestKit.dir")))

    List pluginClasspath

    File buildFile
    File gradleSettings

    def setup() {
        pluginClasspath = getClass().classLoader.findResource('plugin-classpath.txt').readLines().collect { new File(it) }

        buildFile = testProjectDir.newFile('build.gradle')
        gradleSettings = testProjectDir.newFile('settings.gradle')
        gradleSettings.write('rootProject.name = \'activejdbc-gradle-plugin-test-project\'')
        log.debug "buildFile=$buildFile"
    }

    protected GradleRunner createGradleRunner(String taskName) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(pluginClasspath)
                .withArguments(taskName, '--info', '--stacktrace')
        // .withDebug(true)
    }
}
