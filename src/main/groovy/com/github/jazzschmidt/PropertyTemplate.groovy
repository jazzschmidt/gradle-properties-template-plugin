package com.github.jazzschmidt

class PropertyTemplate {

    String name, defaultValue, comment

    static PropertyTemplate parseTemplate(String line) {
        def pattern = ~/^([^=]+)=([^#]*)#?(.*)$/
        def matcher = line =~ pattern

        matcher.find()

        new PropertyTemplate(
                name: matcher.group(1).trim(),
                defaultValue: matcher.group(2).trim(),
                comment: matcher.group(3).trim()
        )
    }

    String format() {
        def text = name
        List<String> appendix = []

        if (!comment.empty) {
            appendix += comment
        }

        if (!defaultValue.empty) {
            appendix += "default: $defaultValue"
        }

        if (!appendix.empty) {
            text += " (${appendix.join(', ')})"
        }

        return text
    }
}