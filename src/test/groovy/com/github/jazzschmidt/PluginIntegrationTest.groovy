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
        createProject([greetend: 'World # Who shall be greeted', greeting: 'Hello']) {
            """\
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            """
        }

        when:
        def result = buildAndFail()

        then:
        result.output.contains('greetend')
        result.output.contains('greeting')
    }

    def createProject(String name = 'test-project', Map<String, String> templateProperties, Closure cl) {
        settingsFile << "rootProject.name = '$name'"
        templateFile << templateProperties.collect { k, v -> "$k=$v" }.join("\n")
        buildFile << cl()
    }

    def build(List<String> args) {
        createRunner()
                .withArguments(args ?: [])
                .build()
    }

    def buildAndFail(List<String> args) {
        createRunner()
                .withArguments(args ?: [])
                .buildAndFail()
    }

    def createRunner() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withDebug(true)
    }
}
