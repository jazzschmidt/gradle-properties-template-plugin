package com.github.jazzschmidt

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.util.environment.OperatingSystem

import java.util.concurrent.TimeUnit

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

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

    def teardown() {
        testProjectDir.delete()
    }

    def "validateProperties task fails the build on missing properties"() {
        given:
        createProject([greetend: 'World # Who shall be greeted', greeting: 'Hello']) {
            """\
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            
            validateProperties {
                checkGitIgnore = false
            }
            """
        }

        when:
        def result = buildAndFail('validateProperties')

        then:
        result.task(':validateProperties').outcome == FAILED
        result.output.contains('greeted')
        result.output.contains('greeting')
    }

    def "validateProperties task continues the build on present properties"() {
        given:
        createProject([greetend: 'World # Who shall be greeted', greeting: 'Hello']) {
            """\
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            
            validateProperties {
                checkGitIgnore = false
            }
            """
        }

        when:
        def result = build('validateProperties', '-Pgreetend=World', '-Pgreeting=Hello')

        then:
        result.task(':validateProperties').outcome == SUCCESS
        !result.output.contains('greeted')
        !result.output.contains('greeting')
    }

    def "plugin can be applied to multi-module project"() {
        given:
        createProject([:]) {
            """\
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            
            validateProperties {
                checkGitIgnore = false
            }
            """
        }
        createSubproject('subproject') { "" }

        when:
        def result = build('validateProperties')

        then:
        result.task(':validateProperties').outcome == SUCCESS
    }

    def "plugin can be applied to parameterized multi-module project"() {
        given:
        createProject([:]) {
            """\
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            
            repositories {
                mavenCentral()
            }
            
            ext {
                JUNIT_VERSION = '4.12'
            }
            
            validateProperties {
                checkGitIgnore = false
            }
            """
        }
        createSubproject('subproject') {
            '''\
            plugins {
                id 'java'
            }
            
            dependencies {
                implementation "junit:junit:${JUNIT_VERSION}"
            }
            '''
        }

        when:
        def result = build('validateProperties')

        then:
        result.task(':validateProperties').outcome == SUCCESS
    }

    def "validateProperties fails and emits warning when gradle.properties is versioned"() {
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
        def result = buildAndFail('validateProperties')

        then:
        result.task(':validateProperties').outcome == FAILED
        result.output.contains('gradle.properties')
        result.output.contains('security leak')
    }

    def "validateProperties continues when gradle.properties is ignored by git"() {
        given:
        createProject([:]) {
            """\
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            """
        }

        testProjectDir.newFile('gradle.properties') << '# intentionally left empty'
        testProjectDir.newFile('.gitignore') << 'gradle.properties'

        when: 'adding gradle.properties to a git repository'
        def proc = 'git init'.execute([], testProjectDir.root)
        proc.waitFor(200L, TimeUnit.MILLISECONDS)

        and:
        def result = build('validateProperties')

        then:
        result.task(':validateProperties').outcome == SUCCESS
    }

    def "fails the build when template file does not exist"() {
        given:
        createProject([:]) {
            """\
            plugins {
                id 'com.github.jazzschmidt.properties-template-plugin'
            }
            """
        }

        when:
        templateFile.delete()

        and:
        def result = buildAndFail('validateProperties')

        then:
        result.task(':validateProperties').outcome == FAILED
    }

    def createProject(String name = 'test-project', Map<String, String> templateProperties, Closure cl) {
        settingsFile << "rootProject.name = '$name'\n"
        templateFile << templateProperties.collect { k, v -> "$k=$v" }.join("\n")
        buildFile << cl()
    }

    def createSubproject(String name, Closure cl) {
        testProjectDir.newFolder(name)
        settingsFile << "include(':$name')\n"
        testProjectDir.newFile("$name/build.gradle") << cl()
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
