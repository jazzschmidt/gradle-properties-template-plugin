package com.github.jazzschmidt

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.TimeUnit

class ValidateProperties extends DefaultTask {

    @InputFile
    File template

    boolean checkGitIgnore = true

    Parser templateParser = new DefaultPropertiesParser()

    @Override
    String getDescription() {
        "Validates project properties against a template"
    }

    @TaskAction
    def validate() {
        validateProperties()

        if (checkGitIgnore) validateGitStatus()
    }

    private void validateProperties() {
        List<PropertyTemplate> propertyTemplates = templateParser.parseTemplate(template)
        List<PropertyTemplate> missingProperties = propertyTemplates.findAll {
            !project.hasProperty(it.name)
        }

        if (!missingProperties.empty) {
            String error = "The following properties must be set before building:\n" +
                    missingProperties.collect { " - ${it.format()}" }.join("\n")
            throw new GradleException(error)
        }
    }

    private void validateGitStatus() {
        def proc = "git status --ignored --short".execute([], project.projectDir)
        proc.waitFor(200L, TimeUnit.MILLISECONDS)

        if (proc.exitValue() != 0) {
            def error = 'Could not check git status of gradle.properties'
            throw new GradleException(error)
        }

        if (!proc.text.contains('!! gradle.properties')) {
            def error = 'Potential security leak: File gradle.properties is versioned under git'
            throw new GradleException(error)
        }
    }
}
