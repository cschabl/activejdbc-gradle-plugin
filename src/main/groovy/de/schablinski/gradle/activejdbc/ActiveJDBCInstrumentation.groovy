/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.schablinski.gradle.activejdbc

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.process.JavaExecSpec
import org.gradle.process.ProcessExecutionException
import org.javalite.instrumentation.Main

import javax.inject.Inject

/**
 * Gradle task for performing ActiveJDBC instrumentation on a set of compiled {@code .class} files.
 * @Deprecated since 3.1.0
 */
abstract class ActiveJDBCInstrumentation extends DefaultTask {

    /** The directory containing class files to be instrumented. */
    @InputDirectory
    String classesDir

    /** The output directory to write back classes after instrumentation. */
    @OutputDirectory
    String outputDir

    @Inject
    protected abstract ExecOperations getExecOperations()

    @Inject abstract ObjectFactory getObjectFactory()

    @InputFiles
    abstract ConfigurableFileCollection getGetActiveJdbcDependencies()

    @Inject
    ActiveJDBCInstrumentation() {
        description = "Instrument compiled class files extending from 'org.javalite.activejdbc.Model'"
    }

    @TaskAction
    def instrument() {
        logger.debug "ActiveJDBCInstrumentation.instrument"

        FileCollection instrumentationClasspath = getGetActiveJdbcDependencies()

        if (!classesDir) {
            throw new GradleException("classesDir is not specified")
        }

        instrumentationClasspath += getObjectFactory().fileCollection().from(classesDir)

        logger.info "Instrumenting ActiveJDBC model classes under $classesDir"

        runInstrumentation(instrumentationClasspath, outputDir ?: classesDir)
    }

    private void runInstrumentation(FileCollection instrumentationClasspath, String outputDirpath) {
        String slf4JLogLevel = GradleUtils.getSlf4LogLevel(logger)
        StdOutCaptor stdOutCaptor = StdOutCaptor.newInstance()
        StdOutCaptor stdErrCaptor = StdOutCaptor.newInstance()

        try {
            getExecOperations().javaexec { JavaExecSpec jes ->
                logger.info "Running ActiveJDBC instrumentation instance with environment: ${jes.environment}"

                jes.classpath = instrumentationClasspath
                jes.mainClass.set(Main.canonicalName)
                jes.systemProperties = ['outputDirectory'                       : outputDirpath,
                                        'org.slf4j.simpleLogger.defaultLogLevel': slf4JLogLevel]

                jes.standardOutput = stdOutCaptor
                jes.errorOutput = stdErrCaptor
            }
        }
        catch (ProcessExecutionException ex) {
            throw ex
        }
        finally {
            if (stdOutCaptor.getContent()) {
                logger.info stdOutCaptor.getContent()
            }
            if (stdErrCaptor.getContent()) {
                logger.error stdErrCaptor.getContent()
            }
        }
    }
}
