package de.schablinski.gradle.activejdbc

import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Log4j2
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

@Log4j2
class ActiveJDBCGradlePluginTest extends AbstractActiveJDBCGradlePluginTest {

    private static final String BUILD_FILE_TEMPLATE =
            '''

plugins {
    id 'java'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

dependencies {
    compile 'org.javalite:activejdbc:2.3'
}

repositories {
    mavenCentral()
}
        '''


    private static final String BUILD_FILE_TOOL_VERSION =
            '''

plugins {
    id 'java'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

activejdbc.toolVersion = '$version'

dependencies {
    compile 'org.javalite:activejdbc:$version'
}

repositories {
    mavenCentral()
}
        '''

    File javaDir

    def setup() {
        File srcDir = testProjectDir.newFolder('src')
        javaDir = new File(new File(srcDir, 'main'), 'java')
        javaDir.mkdirs()
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
        result.getOutput() =~ /Instrumented class.*Address\.class/

        log.debug "Gradle output: " + result.getOutput()
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
        result.getOutput() =~ /Skipping task ':compileJava'/

        log.debug "Gradle output: " + result.getOutput()
    }

    def "should load specified tool version" () {
        given:
        def givenToolVersion = '2.0'

        buildFile << createBuildScript(givenToolVersion)

        AntBuilder ant = new AntBuilder()
        ant.copy(file : 'src/test/resources/Address.java', toDir: javaDir)

        when:
        BuildResult result = createGradleRunner('classes').build()

        then:
        result.task(":classes").outcome == TaskOutcome.SUCCESS
        result.getOutput() =~ /Starting process.*activejdbc-instrumentation-${givenToolVersion}.jar.*/
        !(result.getOutput() =~ /Starting process.*activejdbc-instrumentation-2.2.jar.*/)

        log.debug "Gradle output: " + result.getOutput()
    }

    def "should use ActiveJDBC version 2.3.1-j8" () {
        given:
        def givenToolVersion = '2.3.1-j8'

        buildFile << createBuildScript(givenToolVersion)

        AntBuilder ant = new AntBuilder()
        ant.copy(file : 'src/test/resources/Address.java', toDir: javaDir)

        when:
        BuildResult result = createGradleRunner('classes').build()

        then:
        result.task(":classes").outcome == TaskOutcome.SUCCESS
        result.getOutput() =~ /Starting process.*activejdbc-instrumentation-${givenToolVersion}.jar.*/

        log.debug "Gradle output: " + result.getOutput()
    }

    private static def createBuildScript(def version) {
        def templEngine = new SimpleTemplateEngine()
        templEngine.createTemplate(BUILD_FILE_TOOL_VERSION).make([version: version ])
    }
}
