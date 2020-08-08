package com.github.jazzschmidt

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.model.ConfigurationCycleException

class ValidateProperties extends DefaultTask {

    @InputFile
    File template

    @TaskAction
    def validate() {
        List<String> missingProperties = []

        template.text.readLines().each { String line ->
            def property = createPropertyTemplate(line)

            if (!project.hasProperty(property.name)) {
                missingProperties += property as String
            }
        }

        if (!missingProperties.empty) {
            String error = "The following properties must be set before building:\n" +
                    missingProperties.collect { " - $it" }.join("\n")
            fail(error)
        }
    }

    private void fail(String msg) {
        throw new ConfigurationCycleException(msg)
    }

    protected PropertyTemplate createPropertyTemplate(String line) {
        def pattern = ~/^([^=]+)=([^#]*)#?(.*)$/
        def matcher = line =~ pattern

        matcher.find()

        new PropertyTemplate(
                name: matcher.group(1).trim(),
                defaultValue: matcher.group(2).trim(),
                comment: matcher.group(3).trim()
        )
    }
}
