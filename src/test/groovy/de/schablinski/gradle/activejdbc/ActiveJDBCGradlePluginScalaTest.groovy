package de.schablinski.gradle.activejdbc

import groovy.util.logging.Log4j2
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

@Log4j2
class ActiveJDBCGradlePluginScalaTest extends AbstractActiveJDBCGradlePluginTest {

    private static final String BUILD_FILE_TEMPLATE =
            '''

plugins {
    id 'scala'
    id 'de.schablinski.activejdbc-gradle-plugin'
}   

instrumentModels {
    // assuming Gradle 4.0 or higher
    classesDir = project.sourceSets.main.scala.outputDir.getPath()
    outputDir = classesDir
}

compileScala.doLast {
    instrumentModels.instrument()
}

dependencies {
    compile 'org.scala-lang:scala-library:2.12.6'
    compile 'org.javalite:activejdbc:2.2'
    activejdbc 'org.scala-lang:scala-library:2.12.6'
}

repositories {
    jcenter()
}
        '''

    File scalaDir

    def setup() {
        File srcDir = testProjectDir.newFolder('src')
        scalaDir = new File(new File(srcDir, 'main'), 'scala')
        scalaDir.mkdirs()
    }

    def "should instrument scala model classes"() {
        given:
        buildFile << BUILD_FILE_TEMPLATE

        AntBuilder ant = new AntBuilder()
        ant.copy(file : 'src/test/resources/Person.scala', toDir: scalaDir)

        when:
        BuildResult result = createGradleRunner('classes').build()

        then:
        result.task(":classes").outcome == TaskOutcome.SUCCESS
        result.getOutput() =~ /Instrumented class.*Person\.class/

        log.info "Gradle output: " + result.getOutput()
    }
}
