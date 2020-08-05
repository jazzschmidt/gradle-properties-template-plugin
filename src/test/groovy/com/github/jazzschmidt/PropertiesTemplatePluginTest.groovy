package com.github.jazzschmidt

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PropertiesTemplatePluginTest extends Specification {

    def 'applying the plugin adds configuration and init task'() {
        given:
        def project = project

        when:
        project.pluginManager.apply('com.github.jazzschmidt.properties-template-plugin')

        then:
        project.extensions.findByName(PropertiesTemplateExtension.EXTENSION_NAME)
        project.tasks.findByName('initializeProperties')
    }

    Project getProject() {
        ProjectBuilder.builder().build()
    }

}
