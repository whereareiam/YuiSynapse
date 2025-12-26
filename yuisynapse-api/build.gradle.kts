plugins {
    id("maven-publish")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "YuiSynapse"
            pom {
                name.set("YuiSynapse")
                description.set("Public API for YuiSynapse - Your gateway to AI")
            }
        }
    }
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        title = "YuiSynapse API"
        windowTitle = "YuiSynapse API"
    }
}

extensions.configure<PublishingExtension> {
    repositories {
        maven {
            val realm = (System.getenv("PUBLISH_REALM")
                ?: if ((System.getenv("VERSION") ?: "dev").contains("dev", true)) "development" else "release")
                .lowercase()
            url = uri("https://maven.whereareiam.me/$realm")
            credentials {
                username = System.getenv("PUBLISH_USER") ?: ""
                password = System.getenv("PUBLISH_TOKEN") ?: ""
            }
        }
    }
}