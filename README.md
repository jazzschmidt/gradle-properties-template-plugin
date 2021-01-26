# Gradle Properties Template Plugin ![CI with Gradle](https://github.com/jazzschmidt/gradle-properties-template-plugin/workflows/CI%20with%20Gradle/badge.svg?branch=master) <a href="https://plugins.gradle.org/plugin/com.github.jazzschmidt.properties-template-plugin"><img src="https://img.shields.io/badge/Gradle%20Plugin-1.0.1-brightgreen" /></a>

Ensures that all required project properties are present and not versioned under Git
to prevent accidental security leaks. Missing properties are listed in a human-readable
manner.

Since the build is usually extended over and over again in long-term projects, this
plugin supports integrating new project properties with a fail-fast approach. 

## Usage

This Plugin is published to the Gradle Plugin Portal and can be included with:
```groovy
plugins {
    id "com.github.jazzschmidt.properties-template-plugin" version "1.0.1"
}
```


Create a `gradle.template.properties` file that reflects all required properties for your build,
assign default values and add a comment in the line before if you like:

```properties
# The item that shall be greeted
custom.prop.name = World
custom.prop.greeting = Hello ${custom.prop.name}

# Incrceases the verbosity of the greeting
custom.prop.verbose = true

# Secret password
secret=
```

When running the build with missing properties, the plugin then emits this error:
```
$ ./gradlew build -Pcustom.prop.verbose=false
> Task :validateProperties FAILED

FAILURE: Build failed with an exception.

* What went wrong:
A problem occurred configuring root project 'my-project'.
> The following properties must be set before building:
   - custom.prop.name (The item that shall be greeted, default: World)
   - custom.prop.greeting (default: Hello ${custom.prop.name})
   - secret (Secret password)
```

*Notice:* Only single lines with an colon assignment (`key=value`) are considered, but
you are free to implement a custom template parser.

### Git Status Cheking

When the `gradle.properties` file is not ignored by Git and the task is not
otherwise configured, the build also fails with an exception. This way every developer
can keep his custom properties in the local project folder itself.

```
* What went wrong:
Execution failed for task ':validateProperties'.
> Potential security leak: File gradle.properties is versioned under git
```

## Configuration

The `validateProperties` task uses per default the `gradle.template.properties` file
and checks wheter the final `gradle.properties` file is ignored by Git, but the task
is configurable:
```groovy
validateProperties {
    // Use a different template
    template = file('template.properties')
    // Do not check Git status for gradle.properties
    checkGitIgnore = false
    
    // Usage of a custom template parser
    templateParser = new CustomParser()
}
```
