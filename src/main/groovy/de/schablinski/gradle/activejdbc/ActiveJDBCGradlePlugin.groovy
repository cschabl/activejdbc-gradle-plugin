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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.tasks.JvmConstants
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.scala.ScalaPlugin


/**
 * Gradle plugin the registers an {@link ActiveJDBCInstrumentation} task to run as part of the classes step.
 */
class ActiveJDBCGradlePlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'activejdbc'

    private Properties buildTimeProps

    @Override
    void apply(Project project) {
        if (GradleUtils.getGradleMajorVersion(project) < 9) {
            throw new GradleException("This version of plugin activejdbc-gradle-plugin requires Gradle 9 oder higher. " +
                    "Use activejdbc-gradle-plugin 2.0.4 for Gradle 7.x - 8.x.")
        }

        loadBuildTimeProperties()

        String defaultToolVersion = buildTimeProps.getProperty("activejdbc-version")
        ActiveJDBCExtension activeJdbcExtension = project.extensions.create(EXTENSION_NAME, ActiveJDBCExtension)

        if (!project.getPluginManager().hasPlugin("java")) {
            project.logger.debug "ActiveJDBCGradlePlugin.apply: java plugin has not been applied"
        }

        String compileActiveJdbcVersion
        def getActiveJdbcVersion = { dependencies ->
            Dependency activeJdbcDep = dependencies.find { d -> d.group == "org.javalite" && d.name == "activejdbc"}
            if (activeJdbcDep) {
                compileActiveJdbcVersion = activeJdbcDep.version
                project.logger.debug "ActiveJDBCPlugin.getActiveJdbcVersion: version=$compileActiveJdbcVersion"
            }
        }

        project.plugins.withId("java") {
            project.configurations.named(JvmConstants.IMPLEMENTATION_CONFIGURATION_NAME) {
                 withDependencies(getActiveJdbcVersion)
            }
        }

        Configuration activeJdbcConfig = project.configurations.maybeCreate("activejdbc")
        activeJdbcConfig.setDescription("The ActiveJDBC libraries to be used for this project.")
        activeJdbcConfig.withDependencies { dependencies ->
            def activeJdbcVersion = defaultToolVersion

            if (activeJdbcExtension.toolVersion) {
                project.logger.info "ActiveJDBCGradlePlugin: using explicit tool version=" + activeJdbcExtension.toolVersion
                activeJdbcVersion = activeJdbcExtension.toolVersion
            }
            else if (compileActiveJdbcVersion) {
                project.logger.info "ActiveJDBCPlugin: using ActiveJDBC version $compileActiveJdbcVersion from compileClasspath"
                activeJdbcVersion = compileActiveJdbcVersion
            }

            dependencies.add(project.dependencies.create("org.javalite:activejdbc-instrumentation:" + activeJdbcVersion))
            dependencies.add(project.dependencies.create("org.javalite:activejdbc:" + activeJdbcVersion))
            dependencies.add(project.dependencies.create("org.slf4j:slf4j-simple:" + buildTimeProps.get("slf4j-simple-version")))
        }

        project.plugins.withType(JavaPlugin).configureEach {

            def instrumentModels = project.tasks.register('instrumentModels', ActiveJDBCInstrumentation) {
                classesDir = GradleUtils.getJavaMainOutputDir(project)
                group = "build"
            }

            // use it as doLast action, because Gradle takes hashes of class files for incremental build afterwards
            project.tasks.named(JvmConstants.COMPILE_JAVA_TASK_NAME) {
                doLast {
                    instrumentModels.get().instrument()
                }
            }
        }

        project.plugins.withType(GroovyPlugin).configureEach {
            def instrumentGroovyModels = project.tasks.register('instrumentGroovyModels', ActiveJDBCInstrumentation) {
                classesDir = project.sourceSets.main.groovy.classesDirectory.getAsFile().get()
                group = "build"
            }
            // use it as doLast action, because Gradle takes hashes of class files for incremental build afterwards
            project.tasks.named("compileGroovy") {
                doLast {
                    instrumentGroovyModels.get().instrument()
                }
            }
        }

        project.plugins.withType(ScalaPlugin).configureEach {
            def instrumentScalaModels = project.tasks.register('instrumentScalaModels', ActiveJDBCInstrumentation) {
                classesDir = project.sourceSets.main.scala.classesDirectory.getAsFile().get()
                group = "build"
             }
            // use it as doLast action, because Gradle takes hashes of class files for incremental build afterwards
            project.tasks.named("compileScala") {
                doLast {
                    instrumentScalaModels.get().instrument()
                }
            }
        }
    }

    private void loadBuildTimeProperties() {
        URL url = ActiveJDBCGradlePlugin.class.getClassLoader().getResource("activejdbc-gradle-plugin.properties")
        InputStream input = null

        try {
            input = url.openStream()
            buildTimeProps = new Properties()
            buildTimeProps.load(input)
        }
        finally {
            input?.close()
        }
    }

}
