# gradle-debian-plugin
Package a java application for Debian GNU/Linux

## About
This gradle plugin should serve as a convenient way to extend the [gradle application plugin](https://docs.gradle.org/current/userguide/application_plugin.html) to create a Debian binary package. A debian binary package contains all the executable files, libraries, and documentation needed to run your application in a Debian GNU/Linux environment. Read more about Debian packages in [the manual](https://www.debian.org/doc/manuals/debian-faq/pkg-basics.en.html).

There are other debian packaging plugins out there. This implementation aims to provide the fast utility of a convention over configuration approach. There are some configurations that will improve your quality of life, but you can get up and running in short order.

## Usage
The project [examples](examples) provide a good reference for recommended usage. There are currently zero mandatory configurations, so all you have to do is add the plugin ID to your build.gradle as follows.

```
plugins {
    id 'application'
    id 'io.github.tbordovsky.debian'
}

application {
    mainClass = 'org.gradle.sample.Main'
}
```

You can also make additional configurations using the debian plugin DSL.

```
debian_packaging {
    provisioningDirectory = file('provisioning/root')
    installPath = '/opt'
    postInstallFile = file('provisioning/debian/postinst')
    preUninstallFile = file('provisioning/debian/prerm')

    packageName = 'hello-world'
    version = '1.0'
    section = 'java'
    priority = 'optional'
    architecture = 'all'
    maintainer = '<root@localhost>'
    description = 'A single line synopsis'
}
```

Run `./gradlew buildDeb` to create the artifact.

## Running Java Applications As A Service
[systemd](https://systemd.io/) is the ideal linux integration for this kind of artifact. Create a service unit configuration and include it in the debian plugin "provisioningDirectory" and you should be able to manage it like your other Linux services. Once again, the [examples](examples) are a good reference.
