package com.github.jazzschmidt

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class PluginIntegrationTest extends Specification {

    TemporaryFolder testProjectDir = new TemporaryFolder()
    File settingsFile
    File buildFile
    File templateFile

    def setup() {
        testProjectDir.create()
        settingsFile = testProjectDir.newFile('settings.gradle')
        buildFile = testProjectDir.newFile('build.gradle')
        templateFile = testProjectDir.newFile('gradle.template.properties')
    }

    def "shows missing properties"() {
        given:
        settingsFile << "rootProject.name = 'test-project'"
        buildFile << """
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            
            task helloWorld {
                doLast {
                    println 'Hello world!'
                }
            }
        """

        templateFile << """\
        custom.prop.name=World # Who shall be greeted?
        custom.prop.greeting=Hello
        """.stripIndent()

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('helloWorld')
                .withPluginClasspath()
                .withDebug(true)
                .buildAndFail()

        then:
        result.output.contains('custom.prop.name')
        result.output.contains('custom.prop.greeting')
    }
}
