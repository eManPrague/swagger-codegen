import org.gradle.api.Project

fun Project.findPropertyOrNull(s: String) = findProperty(s) as String?