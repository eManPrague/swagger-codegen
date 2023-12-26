plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka) apply false
}

allprojects {
    group = "cz.eman.swagger"
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = libs.versions.gradle.get()
    distributionType = Wrapper.DistributionType.BIN
}
