package com.github.jazzschmidt

class PropertyTemplate {

    String name, defaultValue, comment

    String format() {
        def text = name
        List<String> appendix = []

        if (comment) {
            appendix += comment
        }

        if (defaultValue) {
            appendix += "default: $defaultValue"
        }

        if (appendix) {
            text += " (${appendix.join(', ')})"
        }

        return text
    }
}
