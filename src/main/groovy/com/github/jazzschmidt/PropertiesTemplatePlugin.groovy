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
                fail()
            }

            extension.template.text.readLines().each { String line ->
                String property = line.tokenize('=').first()

                if (!project.hasProperty(property)) {
                    fail(property)
                }
            }
        }
    }

    void fail(String p = '') {
        throw new ConfigurationCycleException("Property $p not set!")
    }

}
