package com.github.jazzschmidt

import org.gradle.api.Plugin
import org.gradle.api.Project

class PropertiesTemplatePlugin implements Plugin<Project> {

    @Override
    void apply(final Project project) {
        project.extensions.create(PropertiesTemplateExtension.EXTENSION_NAME, PropertiesTemplateExtension)

        project.task('initializeProperties')
    }

}
