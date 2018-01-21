Set of plugins for [Android Studio](https://developer.android.com/studio/index.html) that add the functionality to show reports from gradle metric tasks, such as [PMD](https://docs.gradle.org/current/userguide/pmd_plugin.html) and [Checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html), the main plugin adds the ability to run a task that will execute the appropriate gradle tasks and parse their output and show it in [Android Studio](https://developer.android.com/studio/index.html).

----------
1. [Requirements](#requirements)
1. [Installation](#installation)
1. [Usage](#usage)
1. [How to build](#how-to-build)
1. [Contributor API](#contributor-api)

----------
![Image of and example output](https://raw.githubusercontent.com/DrakkLord/gradle-android-metric-plugin/webmisc/images/gradle-metric-plugin-example.png)

## [Requirements](#requirements) ##

- [Android Studio](https://developer.android.com/studio/index.html) version **3.0** or newer is required!
- [Gradle](https://gradle.org/) version **4.0** or **newer** is required! It doesn't matter if gradle is being used from wrapper or globally installed but it must be **4.0** or **newer**.
- at least one contributor plugin must be installed, see [installation](#installation) section for details
- because gradle metric tasks are being used in order for the plugin to report anything the project being used must have at least one project with [PMD](https://docs.gradle.org/current/userguide/pmd_plugin.html) or [Checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html) plugin applied and the corresponding contributor plugin installed in Android Studio, see [usage](#usage) for details

## [Installation](#installation) ##

You can install the pugins from the jetbrains repository within Android Studio : https://www.jetbrains.com/help/idea/installing-updating-and-uninstalling-repository-plugins.html

In android studio search for the following names and install them:
- [Android Gradle Metrics](https://plugins.jetbrains.com/plugin/9196-android-gradle-metrics) - this is the main plugin, it doesn't work on it's own it requires at least one contributor plugin
- [Android Gradle Metrics - Checkstyle](https://plugins.jetbrains.com/plugin/9197-android-gradle-metrics--checkstyle) - contributor plugin that processes [Checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html) reports
- [Android Gradle Metrics - PMD ](https://plugins.jetbrains.com/plugin/9198-android-gradle-metrics--pmd) - contributor plugin that processes [PMD](https://docs.gradle.org/current/userguide/pmd_plugin.html) reports

You can verify that you insalled correctly by:

- you should see 2 new menu items under 'Analyze':
   - 'Get code metrics report'
   - 'Code metrics plugin status'
- click on menu -> 'Analyze' -> 'Code metrics plugin status', this will show you a window that either shows you errors or shows you what contributor plugins you have installed

## [Usage](#usage) ##

The plugin requires your project to have at least one project with one gradle metric plugin such as [PMD](https://docs.gradle.org/current/userguide/pmd_plugin.html) or [Checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html)

**IMPORTANT**

Dynamic resolution of configuration is *NOT* supported, for example setting the destination from a variable won't be recognised by the plugin and it will revert to the default folder, workaround is to use string literals.
```
def variable = "$buildDird/whatever.html"
checkstyleTask {
  reports {
    html {
      destination variable // plugin won't be able to use this
      destination "reports/checkstyle/whatever.html" // this is okay
    }
  }
}
```

You can add PMD and Checkstyle to your gradle project by:

```
apply plugin: 'checkstyle'
apply plugin: 'pmd'

checkstyle {
    toolVersion = '8.1' // optionally specify tool version
}

pmd {
    toolVersion = '5.8.1' // optionally specify tool version
}

tasks.withType(Checkstyle) {
    ignoreFailures = true // recommended because the plugin will report only up to the point the first issue if this is not set
    showViolations = false
    
    reports {
        xml.enabled = true  // REQUIRED the plugin parses xml reports so this is essential
    }
    
    include '**/*.java'
}

tasks.withType(Pmd) {
    ignoreFailures = true // recommended because the plugin will report only up to the point the first issue if this is not set

    reports {
        xml.enabled = true  // REQUIRED the plugin parses xml reports so this is essential
    }

    include '**/*.java'
}
```

After this you can verify that the reporting works in gradle by executing:

```
gradle pmd checkstyle
```

the default output of the check tools is *projectDirectory/build/reports/pmd/* and *projectDirectory/build/reports/checkstyle/* respectively.

Now open your project in Android Studio and click on menu > 'Analyze' > 'Get code metrics report', this will execute the appropriate gradle tasks and when they are done open a menu with the results.
You should see either 'no issues' or a list of issues, ideally you should see the same errors as you see in the xml files generated by manually running the tasks.

## [How to build](#how-to-build) ##

[IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/#section=windows) and an [Android Studio](https://developer.android.com/studio/index.html) version **3.0** or **newer** installation is required for building.

- open the project in IntelliJ
- setup a plugin SDK that points to the Android Studio installation
- make sure that the plugin SDK uses the JDK from the Android Studio installation, you may have to make a JDK configuration first to be able to set it for the plugin SDK
- you need to set the following additional jar's in the plugin SDK's classpath :
    - Android Studio/plugins/android/lib/artwork.jar
    - Android Studio/plugins/android/lib/android.jar
    - Android Studio/plugins/android/lib/build-common.jar
    - Android Studio/plugins/gradle/lib/gradle-tooling-extension-api.jar
    - Android Studio/gradle/gradle-4.1/lib/gradle-tooling-api-4.1.jar
    - Android Studio/plugins/gradle/lib/gradle.jar
    - Android Studio/gradle/gradle-4.1/lib/plugins/gradle-code-quality-4.1.jar
    - Android Studio/gradle/gradle-4.1/lib/plugins/gradle-reporting-4.1.jar
    - Android Studio/gradle/gradle-4.1/lib/gradle-core-4.1.jar
    - Android Studio/gradle/gradle-4.1/lib/gradle-base-services-groovy-4.1.jar
- at this point the plugin should compile
- for testing and debugging you can make run configurations for each of the 3 plugins, in order to get a working environemnt first start the core plugin, then close android studio then switch to one of the contributor plugins start with that this way you can install the plugins into the test environment

## [Contributor API](#contributor-api) ##

The main plugin does not include any implementation for specific gradle metric plugins, instead it exposes a 'Contributor API' which simply allows individual plugins to handle the specific gradle metric plugins.

[GradleMetricContributor - contributor plugin interface](https://github.com/DrakkLord/gradle-android-metric-plugin/blob/master/plugin/metric-contributor-api/src/com/drakklord/gradle/metric/core/contributor/GradleMetricContributor.java)

[GradleMetricCheckstyleContributor - implementation of the interface for checkstyle](https://github.com/DrakkLord/gradle-android-metric-plugin/blob/master/plugin/module_checkstyle/src/com/drakklord/gradle/metric/checkstyle/contributor/GradleMetricCheckstyleContributor.java)
