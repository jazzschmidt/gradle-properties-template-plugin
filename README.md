# Gradle Properties Template Plugin ![CI with Gradle](https://github.com/jazzschmidt/gradle-properties-template-plugin/workflows/CI%20with%20Gradle/badge.svg?branch=master) <a href="https://plugins.gradle.org/plugin/com.github.jazzschmidt.properties-template-plugin"><img src="https://img.shields.io/badge/Gradle%20Plugin-1.0--SNAPSHOT-brightgreen" /></a>

Validates project properties against a template file. Suppose you add a task, that requires a
new project property set to run properly, it is then neccessary to inform your colleagues,
configure CI, document your changes and so on. In long-term projects those are at some point
often a plenty one.

This plugin fails the build immediately after the configuration phase and lists all properties
that should be added, along with their default values and comments if any.

## Usage
Create a `gradle.template.properties` file that reflects all properties needed to run your build,
assign default values and add a comment if you like:

```properties
custom.prop.name=World # The item that shall be greeted
custom.prop.greeting=Hello ${custom.prop.name}
custom.prop.verbose=true # Incrceases the verbosity of the greeting
```

When running the build without a custom `gradle.properties` file, the plugin then emits this error:
```
$ ./gradlew build -Pcustom.prop.verbose=false

FAILURE: Build failed with an exception.

* What went wrong:
A problem occurred configuring root project 'my-project'.
> The following properties must be set before building:
   - custom.prop.name (The item that shall be greeted, default: World)
   - custom.prop.greeting (default: Hello ${custom.prop.name})

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

BUILD FAILED in 1s

```
