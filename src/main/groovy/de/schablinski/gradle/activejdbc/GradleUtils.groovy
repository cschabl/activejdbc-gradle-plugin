package de.schablinski.gradle.activejdbc

import org.gradle.api.Project

class GradleUtils {
    static File getJavaMainOutputDir(Project project)
    {
        return getGradleMajorVersion(project) > 3 ? project.sourceSets.main.java.outputDir
                : project.sourceSets.main.output.classesDir
    }

    static int getGradleMajorVersion(Project project)
    {
        String gradleVersion = project.getGradle().getGradleVersion()
        Integer.valueOf(gradleVersion.substring(0, gradleVersion.indexOf(".")))
    }
}
