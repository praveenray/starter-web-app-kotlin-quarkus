package my.starter.controllers

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.PathSegment
import jakarta.ws.rs.core.Response
import my.starter.libs.Utils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.readBytes

@Path("/static")
class StaticController(
    @ConfigProperty(name ="is-development") val inDev: Boolean,
    val utils: Utils,
) {
    private val logger = Logger.getLogger(this::class.java)
    @Path("/{release}/{files: .*}")
    @GET
    fun readStaticFile(
        @PathParam("release") release: String,
        @PathParam("files") filenames: List<PathSegment>
    ): Response {
        if (filenames.isEmpty()) {
            throw IllegalArgumentException("files are not present")
        }

        logger.debug("serving static (release,file): ($release, ${filenames.joinToString(":")})")
        val output = if (inDev) {
            logger.debug("Current Dir: ${System.getProperty("user.dir")}")
            val file = filenames.fold(
                utils.workingFolder().resolve(Paths.get("src/main/resources/static"))
            ) { p, e -> p.resolve(e.path.trim())}
            logger.debug("resolved file: $file")
            Output(
                headers = mapOf(HttpHeaders.CACHE_CONTROL to "no-store"),
                bytes = file.readBytes(),
            )
        } else {
            val file = "/static/${filenames.joinToString("/")}".removeSuffix("/")
            Output(
                headers = mapOf(
                    HttpHeaders.CACHE_CONTROL to "max-age=31536000" // more than one year is not allowed by http specs
                ),
                bytes = javaClass.classLoader.getResourceAsStream(file).use { it.readBytes() },
            )
        }

        return output.headers.entries.fold(Response.ok()) { r, entry -> r.header(entry.key, entry.value) }
            .entity(output.bytes)
            .header(HttpHeaders.CONTENT_LENGTH, output.bytes.size.toString())
            .header(HttpHeaders.CONTENT_TYPE, mimeType(Paths.get(filenames.last().path)))
            .build()
    }

    private fun mimeType(file: java.nio.file.Path) = when(file.extension.lowercase()) {
        "html" -> MediaType.TEXT_HTML
        "css" ->  "text/css"
        "png" -> "image/png"
        "jpg","jpeg" -> "image/jpeg"
        "js" -> "text/javascript"
        "json" -> MediaType.APPLICATION_JSON
        "xml" -> MediaType.TEXT_XML
        "pdf" -> "application/pdf"
        "csv" -> "text/csv"
        "ico" -> "image/x-icon"
        "woff" -> "font/woff"
        "woff2" -> "font/woff2"
        else -> throw IllegalArgumentException("Unknown file extension : ${file.extension}")
    }
}

data class Output(
    val bytes: ByteArray,
    val headers: Map<String, String>
)
