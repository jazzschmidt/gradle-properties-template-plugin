package com.github.jazzschmidt

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.util.environment.OperatingSystem

import java.util.concurrent.TimeUnit

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

    def "validateProperties task fails the build on missing properties"() {
        given:
        createProject([greetend: 'World # Who shall be greeted', greeting: 'Hello']) {
            """\
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            """
        }

        when:
        def result = buildAndFail('validateProperties')

        then:
        result.output.contains('greeted')
        result.output.contains('greeting')
    }

    @PendingFeature
    def "validateProperties task continues the build on present properties"() {
        given:
        createProject([greetend: 'World # Who shall be greeted', greeting: 'Hello']) {
            """\
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            """
        }

        when:
        def result = build('validateProperties', '-Pgreetend=World', '-Pgreeting=Hello')

        then:
        !result.output.contains('greeted')
        !result.output.contains('greeting')
    }

    @PendingFeature
    def "validateProperties emits warning when gradle.properties is versioned"() {
        given:
        createProject([:]) {
            """\
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            """
        }

        testProjectDir.newFile('gradle.properties') << '# intentionally left empty'

        when: 'adding gradle.properties to a git repository'
        ['git init', 'git add gradle.properties'].each { cmd ->
            def proc = cmd.execute([], testProjectDir.root)
            proc.waitFor(200L, TimeUnit.MILLISECONDS)
        }

        and:
        def result = build('validateProperties')

        then:
        result.output.contains('gradle.properties is under version control')
    }

    def createProject(String name = 'test-project', Map<String, String> templateProperties, Closure cl) {
        settingsFile << "rootProject.name = '$name'"
        templateFile << templateProperties.collect { k, v -> "$k=$v" }.join("\n")
        buildFile << cl()
    }

    def build(String... args) {
        createRunner()
                .withArguments(args)
                .build()
    }

    def buildAndFail(String... args) {
        createRunner()
                .withArguments(args)
                .buildAndFail()
    }

    def createRunner() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withDebug(true)
    }

    boolean isGitAvailable() {
        def which = OperatingSystem.current.windows ? 'WHERE' : 'which'
        return "${which} git".execute().waitFor() == 0
    }
}
