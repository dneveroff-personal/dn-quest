import org.gradle.kotlin.dsl.maven

rootProject.name = "dn-quest"

include("dn-quest-shared")
include("authentication-service")
include("user-management-service")
include("quest-management-service")
include("game-engine-service")
include("team-management-service")
include("notification-service")
include("statistics-service")
include("file-storage-service")
include("api-gateway")

// Use only Maven Central for all projects
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://repo1.maven.org/maven2/") }
    }
}