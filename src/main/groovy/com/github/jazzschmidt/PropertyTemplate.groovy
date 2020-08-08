package com.github.jazzschmidt

class PropertyTemplate {

    String name, defaultValue, comment

    @Override
    String toString() {
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