sourceSets {
    main {
        java {
            srcDirs("src/")
        }
    }
}

eclipse {
    project {
        name = "${rootProject.ext.get("appName")}-core"
    }
}
