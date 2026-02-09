import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    java
    signing
    id("com.vanniktech.maven.publish") version "0.36.0"
}

description = "A library for Minestom placement"
group = "rocks.minestom"
version = "0.1.0"

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

mavenPublishing {
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = true
    ))

    coordinates(group.toString(), project.name, version.toString())
    publishToMavenCentral()
    signAllPublications()

    pom {
        name = project.name
        description = project.description
        url = "https://github.com/vibenilla/placement"

        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                name = "mudkip"
                id = "mudkipdev"
                email = "mudkip@mudkip.dev"
                url = "https://mudkip.dev"
            }
        }

        scm {
            url = "https://github.com/vibenilla/placement"
            connection = "scm:git:git://github.com/vibenilla/placement.git"
            developerConnection = "scm:git:ssh://git@github.com/vibenilla/placement.git"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom:2026.01.08-1.21.11")
}