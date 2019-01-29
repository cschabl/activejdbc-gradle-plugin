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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * Gradle plugin the registers an {@link ActiveJDBCInstrumentation} task to run as part of the classes step.
 */
class ActiveJDBCGradlePlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'activejdbc'

    @Override
    void apply(Project project) {
        ActiveJDBCExtension activeJdbcExtension = project.extensions.create(EXTENSION_NAME, ActiveJDBCExtension)
        activeJdbcExtension.toolVersion = loadToolVersion()

        if (!project.getPluginManager().hasPlugin("java")) {
            project.logger.debug "ActiveJDBCGradlePlugin.apply: java plugin has not been applied"
        }

        project.plugins.withType(JavaPlugin) {
            // use it as doLast action, because Gradle takes hashes of class files for incremental build afterwards
            project.tasks.compileJava.doLast {
                project.logger.info "ActiveJDBCGradlePlugin.doLast: tool version=" + activeJdbcExtension.toolVersion

                def instrumentModels = project.tasks.create('instrumentModels', ActiveJDBCInstrumentation)
                instrumentModels.group = "build"

                instrumentModels.instrument()
            }
        }
    }

    private String loadToolVersion() {
        URL url = ActiveJDBCGradlePlugin.class.getClassLoader().getResource("activejdbc-gradle-plugin.properties");
        InputStream input

        try {
            input = url.openStream()
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty("activejdbc-version");
        }
        finally {
            input?.close()
        }
    }
}
