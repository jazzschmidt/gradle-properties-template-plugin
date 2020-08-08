package com.github.jazzschmidt

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ValidatePropertiesTest extends Specification {

    TemporaryFolder folder = new TemporaryFolder()

    Task task

    Project project

    def setup() {
        folder.create()
        project = ProjectBuilder.builder().build()
        task = project.task('validateProperties', type: ValidateProperties) {
            checkGitIgnore = false
        }
    }

    def teardown() {
        folder.delete()
    }

    def "validate fails on missing properties"() {
        given:
        def template = folder.newFile('gradle.template.properties')
        template << """\
        prop1=
        prop2=
        """.stripIndent()

        task.template = template

        when:
        task.validate()

        then:
        thrown(Exception)
    }

    def "validate does not fail when properties are present"() {
        given:
        def template = folder.newFile('gradle.template.properties')
        template << """\
        prop1=
        prop2=
        """.stripIndent()

        project.ext {
            prop1 = 'Hello'
            prop2 = 'World'
        }

        task.template = template

        when:
        task.validate()

        then:
        noExceptionThrown()
    }

}
