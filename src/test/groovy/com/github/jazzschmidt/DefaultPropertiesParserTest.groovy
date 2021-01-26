package com.github.jazzschmidt


import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.util.mop.Use

class DefaultPropertiesParserTest extends Specification {

    TemporaryFolder folder = new TemporaryFolder()
    File template

    Parser parser

    def setup() {
        folder.create()
        template = folder.newFile()
        parser = new DefaultPropertiesParser()
    }

    def teardown() {
        folder.delete()
    }

    @Use(PropertyTemplateValueMatcher)
    def 'parses a full template'() {
        given:
        template << '''\
        ! This template should cover all requirements by now
        
        # url of the service
        service.url=localhost
        service.name = MyService
        
        #secret password
        password=
        '''.stripIndent()

        when:
        def properties = parser.parseTemplate(template)

        then:
        properties.get(0) == new PropertyTemplate(name: 'service.url', defaultValue: 'localhost', comment: 'url of the service')
        properties.get(1) == new PropertyTemplate(name: 'service.name', defaultValue: 'MyService')
        properties.get(2) == new PropertyTemplate(name: 'password', comment: 'secret password')
    }

    private class PropertyTemplateValueMatcher {
        static boolean equals(PropertyTemplate a, PropertyTemplate b) {
            a.name == b.name && a.defaultValue == b.defaultValue && a.comment == b.comment
        }
    }

}
