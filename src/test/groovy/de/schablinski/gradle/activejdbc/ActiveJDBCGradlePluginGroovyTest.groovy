package de.schablinski.gradle.activejdbc

import groovy.util.logging.Log4j2
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

@Log4j2
class ActiveJDBCGradlePluginGroovyTest extends AbstractActiveJDBCGradlePluginTest {

    private static final String BUILD_FILE_TEMPLATE =
            '''

plugins {
    id 'groovy'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

dependencies {
    implementation 'org.codehaus.groovy:groovy:2.4.15'
    implementation 'org.javalite:activejdbc:2.3'
}

repositories {
    jcenter()
}
        '''

    File groovyDir
    File javaDir

    def setup() {
        File srcDir = new File(testProjectDir, 'src')
        groovyDir = new File(new File(srcDir, 'main'), 'groovy')
        groovyDir.mkdirs()
        javaDir = new File(new File(srcDir, 'main'), 'java')
        javaDir.mkdirs()
    }

    def "should instrument groovy model classes"() {
        given:
        buildFile << BUILD_FILE_TEMPLATE

        AntBuilder ant = new AntBuilder()
        ant.copy(file : 'src/test/resources/Company.groovy', toDir: groovyDir)

        when:
        BuildResult result = createGradleRunner('classes').build()

        then:
        result.task(":classes").outcome == TaskOutcome.SUCCESS
        result.getOutput() =~ /Instrumented class.*Company\.class/

        log.info "Gradle output: " + result.getOutput()
    }

    def "should instrument java and groovy model classes"() {
        given:
        buildFile << BUILD_FILE_TEMPLATE

        AntBuilder ant = new AntBuilder()
        ant.copy(file : 'src/test/resources/Address.java', toDir: javaDir)
        ant.copy(file : 'src/test/resources/Company.groovy', toDir: groovyDir)

        when:
        BuildResult result = createGradleRunner('classes').build()

        then:
        result.task(":classes").outcome == TaskOutcome.SUCCESS
        result.getOutput() =~ /Instrumented class.*Address\.class/
        result.getOutput() =~ /Instrumented class.*Company\.class/

        log.info "Gradle output: " + result.getOutput()
    }
}
