package com.github.jazzschmidt

class DefaultPropertiesParser implements Parser {

    private String lastComment

    @Override
    List<PropertyTemplate> parseTemplate(final File template) {
        List<PropertyTemplate> properties = []

        template.text.readLines().each {
            parseLine(it, properties)
        }

        return properties
    }

    private void parseLine(String line, List<PropertyTemplate> properties) {
        if (isAssignment(line)) {
            def (name, defaultValue) = splitAssignment(line)
            properties.add(new PropertyTemplate(name: name, defaultValue: defaultValue, comment: lastComment))
            return
        }

        if (isComment(line)) {
            lastComment = line.substring(1).trim()
        } else {
            lastComment = null
        }
    }

    private String[] splitAssignment(String line) {
        line.split('=', 2)
    }

    private boolean isAssignment(String line) {
        !isComment(line) && line.contains('=')
    }

    private boolean isComment(String line) {
        line.startsWith('#') || line.startsWith('!')
    }
}
