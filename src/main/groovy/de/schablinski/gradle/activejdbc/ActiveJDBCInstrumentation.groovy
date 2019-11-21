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
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec
import org.javalite.instrumentation.Main

/**
 * Gradle task for performing ActiveJDBC instrumentation on a set of compiled {@code .class} files.
 */
class ActiveJDBCInstrumentation extends DefaultTask {

    /** The directory containing class files to be instrumented. */
    String classesDir

    /** The output directory to write back classes after instrumentation. */
    String outputDir

    private FileCollection activeJdbcClasspath

    /**
     * Class path holding the ActiveJDBC library.
     *
     * @return class path holding the ActiveJDBC library
     */
    @Classpath
    FileCollection getActiveJdbcClasspath() {
        if (!activeJdbcClasspath) {
            activeJdbcClasspath = project.configurations.getByName("activejdbc")
        }
        return activeJdbcClasspath
    }

    /**
     * @param activeJdbcClasspath
     *            class path holding the ActiveJdbc library
     */
    void setActiveJdbcClasspath(FileCollection activeJdbcClasspath) {
        this.activeJdbcClasspath = activeJdbcClasspath
    }

    ActiveJDBCInstrumentation() {
        description = "Instrument compiled class files extending from 'org.javalite.activejdbc.Model'"
    }

    @TaskAction
    def instrument() {
        logger.debug "ActiveJDBCInstrumentation.instrument"

        FileCollection instrumentationClasspath = getActiveJdbcClasspath()

        if (!classesDir) {
            File javaMainOutputDir = GradleUtils.getJavaMainOutputDir(project)
            classesDir = javaMainOutputDir.getPath()
            instrumentationClasspath += project.files(javaMainOutputDir)
        }
        else {
            instrumentationClasspath += project.files(classesDir)
        }

        logger.info "Instrumenting ActiveJDBC model classes under $clasesDir"

        runInstrumentation(instrumentationClasspath, outputDir ?: classesDir)
    }

    private void runInstrumentation(FileCollection instrumentationClasspath, String outputDirpath) {
        String slf4JLogLevel = getSlf4LogLevel()

        project.javaexec { JavaExecSpec jes ->
            logger.info "Running ActiveJDBC instrumentation instance with environment: ${jes.environment}"

            jes.classpath = instrumentationClasspath
            jes.main = Main.canonicalName
            jes.systemProperties = ['outputDirectory': outputDirpath,
                                    'org.slf4j.simpleLogger.defaultLogLevel': slf4JLogLevel]
        }
    }

    private String getSlf4LogLevel() {
        if (logger.isTraceEnabled()) return 'trace'
        if (logger.isDebugEnabled()) return 'debug'
        if (logger.isInfoEnabled()) return 'info'
        if (logger.isWarnEnabled()) return 'warn'
        if (logger.isErrorEnabled()) return 'error'
        return 'off'
    }
}
