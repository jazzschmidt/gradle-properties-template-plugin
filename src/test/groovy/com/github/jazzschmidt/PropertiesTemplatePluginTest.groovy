package com.github.jazzschmidt

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class PropertiesTemplatePluginTest extends Specification {


    def 'applying the plugin adds configuration and init task'() {
        given:
        def project = createProject()

        when:
        project.pluginManager.apply(PropertiesTemplatePlugin.PLUGIN_NAME)

        then:
        project.extensions.findByName(PropertiesTemplateExtension.EXTENSION_NAME)
        project.tasks.findByName('initializeProperties')
    }

    def 'aborts when template does not exist'() {
        given:
        def project = createProject()

        when:
        project.pluginManager.apply(PropertiesTemplatePlugin.PLUGIN_NAME)
        project.evaluate()

        then:
        thrown(Exception)
    }

    def 'aborts when properties are not set'() {
        given:
        def project = createProject()
        def propertiesTemplate = project.file('gradle.template.properties')

        propertiesTemplate.createNewFile()
        propertiesTemplate.text = """\
        prop1= # My custom property
        prop2=default # My custom property with a default value
        """.stripIndent()

        when:
        project.pluginManager.apply(PropertiesTemplatePlugin.PLUGIN_NAME)
        project.evaluate()

        then:
        thrown(Exception)
    }

    def 'continues build when properties are set'() {
        given:
        def project = createProject()
        def propertiesTemplate = project.file('gradle.template.properties')

        propertiesTemplate.createNewFile()
        propertiesTemplate.text = """\
        prop1= # My custom property
        prop2=default # My custom property with a default value
        """.stripIndent()

        when:
        project.pluginManager.apply(PropertiesTemplatePlugin.PLUGIN_NAME)
        project.ext {
            prop1 = 'Hello'
            prop2 = 'World'
        }
        project.evaluate()

        then:
        noExceptionThrown()
    }

    Project createProject() {
        ProjectBuilder.builder().build()
    }

    GradleRunner createRunner(Project project) {
        GradleRunner.create().withProjectDir(project.projectDir)
    }

}
