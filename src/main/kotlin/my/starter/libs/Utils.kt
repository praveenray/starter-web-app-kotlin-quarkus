package my.starter.libs

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.LoggerFactory
import java.nio.file.Paths
@ApplicationScoped
class Utils(
    @ConfigProperty(name =  "release.version") private val releaseVersion: String
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    fun workingFolder() = Paths.get(System.getProperty("user.dir"))
    fun static_url(path: String): String {
        val noStatic = path.replace(Regex("^/static"), "").removePrefix("/").removeSuffix("/")
        return "/static/$releaseVersion/$noStatic"
    }
}