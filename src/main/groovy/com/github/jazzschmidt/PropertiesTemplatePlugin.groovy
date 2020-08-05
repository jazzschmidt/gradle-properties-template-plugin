package com.github.jazzschmidt


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.model.ConfigurationCycleException

class PropertiesTemplatePlugin implements Plugin<Project> {

    public static final String PLUGIN_NAME = 'com.github.jazzschmidt.properties-template-plugin'

    @Override
    void apply(final Project project) {
        def extension = project.extensions.create(PropertiesTemplateExtension.EXTENSION_NAME, PropertiesTemplateExtension)
        extension.template = project.file('gradle.template.properties')

        project.task('initializeProperties')

        project.afterEvaluate {
            if (!extension.template.exists()) {
                fail("Properties template file does not exist: ${extension.template.name}")
            }

            List<String> missingProperties = []

            extension.template.text.readLines().each { String line ->
                def property = propertyFromTemplate(line)

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
    }

    void fail(String msg) {
        throw new ConfigurationCycleException(msg)
    }

    ProjectProperty propertyFromTemplate(String line) {
        def pattern = ~/^([^=]+)=([^#]*)#?(.*)$/
        def matcher = line =~ pattern

        matcher.find()

        new ProjectProperty(
                name: matcher.group(1).trim(),
                defaultValue: matcher.group(2).trim(),
                comment: matcher.group(3).trim()
        )
    }

    private class ProjectProperty {

        String name, defaultValue, comment

        @Override
        String toString() {
            def text = name
            List<String> appendix = []

            if (!comment.empty) {
                appendix += comment
            }

            if (!defaultValue.empty) {
                appendix += "default: $defaultValue"
            }

            if (!appendix.empty) {
                text += " (${appendix.join(', ')})"
            }

            return text
        }
    }
}
