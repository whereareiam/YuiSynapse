plugins {
    id("maven-publish")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        title = "YuiSynapse API"
        windowTitle = "YuiSynapse API"
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "me.whereareiam"
            artifactId = rootProject.name
            version = rootProject.version.toString()

            from(components["java"])
        }
    }
}