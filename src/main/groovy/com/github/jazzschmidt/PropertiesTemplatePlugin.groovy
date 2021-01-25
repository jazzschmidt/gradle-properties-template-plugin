package com.github.jazzschmidt

import org.gradle.api.Plugin
import org.gradle.api.Project

class PropertiesTemplatePlugin implements Plugin<Project> {

    @Override
    void apply(final Project project) {
        def task = project.task('validateProperties', type: ValidateProperties, group: 'build') {
            template = project.file('gradle.template.properties')
        }

        project.getTasksByName('assemble', true)*.dependsOn(task)
    }

}
