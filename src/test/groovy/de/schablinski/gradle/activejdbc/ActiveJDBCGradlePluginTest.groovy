package de.schablinski.gradle.activejdbc

import org.apache.logging.log4j.LogManager
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ActiveJDBCGradlePluginTest extends Specification {

    protected static final String TEST_PROPS_FILE = "test.properties"
    protected static final Properties TEST_PROPS = new Properties()

    private static final String BUILD_FILE_TEMPLATE =
            '''

plugins {
    id 'java'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

dependencies {
    compile 'org.javalite:activejdbc:2.2'
}

repositories {
    mavenCentral()
}
        '''

    private static final String BUILD_FILE_ACTIVE_JDBC_2_0 =
            '''

plugins {
    id 'java'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

activejdbc.toolVersion = 2.0

dependencies {
    compile 'org.javalite:activejdbc:2.0'
}

repositories {
    mavenCentral()
}
        '''

    static
    {
        InputStream testPropsStream = ActiveJDBCGradlePluginTest.class.getResourceAsStream("/" + TEST_PROPS_FILE)
        TEST_PROPS.load(testPropsStream)
        testPropsStream.close()
    }

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder(new File(TEST_PROPS.getProperty("gradleTestKit.dir")))

    File buildFile
    File javaDir

    List pluginClasspath

    def setup() {
        File srcDir = testProjectDir.newFolder('src')
        javaDir = new File(new File(srcDir, 'main'), 'java')
        javaDir.mkdirs()

        buildFile = testProjectDir.newFile('build.gradle')
        LogManager.getLogger(getClass()).debug("buildFile=$buildFile")

        pluginClasspath = getClass().classLoader.findResource('plugin-classpath.txt').readLines().collect { new File(it) }
    }

    def "should instrument model classes"() {
        given:
        buildFile << BUILD_FILE_TEMPLATE

        AntBuilder ant = new AntBuilder()
        ant.copy(file : 'src/test/resources/Address.java', toDir: javaDir)

        when:
        BuildResult result = createGradleRunner('classes').build()

        then:
        result.task(":classes").outcome == TaskOutcome.SUCCESS
    }

    def "should not compile again after instrumentation"() {
        given:
        buildFile << BUILD_FILE_TEMPLATE

        AntBuilder ant = new AntBuilder()
        ant.copy(file : 'src/test/resources/Address.java', toDir: javaDir)
        createGradleRunner('classes').build()

        when:
        BuildResult result = createGradleRunner('classes').build()

        then:
        result.task(":classes").outcome == TaskOutcome.UP_TO_DATE
    }

    def "should load specified tool version" () {
        given:
        buildFile << BUILD_FILE_ACTIVE_JDBC_2_0

        AntBuilder ant = new AntBuilder()
        ant.copy(file : 'src/test/resources/Address.java', toDir: javaDir)

        when:
        BuildResult result = createGradleRunner('classes').build()

        then:
        result.task(":classes").outcome == TaskOutcome.SUCCESS
    }

    protected GradleRunner createGradleRunner(String taskName) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(pluginClasspath)
                .withArguments(taskName, '--info', '--stacktrace')
                .forwardOutput()
        .withDebug(true)
    }
}
