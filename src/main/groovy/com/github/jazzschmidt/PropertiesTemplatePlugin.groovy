package com.github.jazzschmidt

import org.gradle.api.Plugin
import org.gradle.api.Project

class PropertiesTemplatePlugin implements Plugin<Project> {

    public static final String PLUGIN_NAME = 'com.github.jazzschmidt.properties-template-plugin'

    @Override
    void apply(final Project project) {
        def task = project.task('validateProperties', type: ValidateProperties) {
            template = project.file('gradle.template.properties')
        }

        project.getTasksByName('assemble', true)*.dependsOn(task)
    }

}
