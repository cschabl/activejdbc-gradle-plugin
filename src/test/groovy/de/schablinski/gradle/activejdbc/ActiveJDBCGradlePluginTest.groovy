package de.schablinski.gradle.activejdbc

import groovy.ant.AntBuilder
import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Log4j2
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.IgnoreIf

@Log4j2
class ActiveJDBCGradlePluginTest extends AbstractActiveJDBCGradlePluginTest {

    private static final String BUILD_FILE_TEMPLATE =
            '''

plugins {
    id 'java'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

dependencies {
    implementation 'org.javalite:activejdbc:2.6-j8'
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
    implementation 'org.javalite:activejdbc:$version'
}

repositories {
    mavenCentral()
}
        '''

    private static final String BUILD_FILE_2_3_1_j8 =
            '''

plugins {
    id 'java'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

dependencies {
    implementation 'org.javalite:activejdbc:2.3.1-j8'
}

repositories {
    mavenCentral()
}
        '''

    File javaDir

    def setup() {
        File srcDir = new File(testProjectDir, 'src')
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
        !(result.getOutput() =~ /Starting process.*activejdbc-instrumentation-2.6-j8.jar.*/)

        log.debug "Gradle output: " + result.getOutput()
    }

    def "should use ActiveJDBC version 2.5-j8" () {
        given:
        def givenToolVersion = '2.5-j8'

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

    def "should use ActiveJDBC 2.3.1-j8 from compileClasspath if toolVersion is not specified" () {
        given:
        def expectedToolVersion = '2.3.1-j8'

        buildFile << BUILD_FILE_2_3_1_j8

        AntBuilder ant = new AntBuilder()
        ant.copy(file : 'src/test/resources/Address.java', toDir: javaDir)

        when:
        BuildResult result = createGradleRunner('classes').build()

        then:
        result.task(":classes").outcome == TaskOutcome.SUCCESS
        result.getOutput() =~ /Starting process.*activejdbc-instrumentation-${expectedToolVersion}.jar.*/

        log.debug "Gradle output: " + result.getOutput()
    }


    @IgnoreIf({ !jvm.java11Compatible })
    def "should instrument with ActiveJDBC 3" () {
        given:
        def givenToolVersion = '3.4-j11'
        buildFile << createBuildScript(givenToolVersion)

        AntBuilder ant = new AntBuilder()
        ant.copy(file : 'src/test/resources/Address.java', toDir: javaDir)

        when:
        BuildResult result = createGradleRunner('classes').build()

        then:
        result.task(":classes").outcome == TaskOutcome.SUCCESS
        result.getOutput() =~ /Starting process.*activejdbc-instrumentation-${givenToolVersion}.jar.*/
    }

    private static def createBuildScript(def version) {
        def templEngine = new SimpleTemplateEngine()
        templEngine.createTemplate(BUILD_FILE_TOOL_VERSION).make([version: version ])
    }
}
