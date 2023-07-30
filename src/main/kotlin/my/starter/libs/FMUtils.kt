package my.starter.libs

import freemarker.template.Configuration
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger
import java.io.StringWriter

@ApplicationScoped
class FMUtils(
    private val fm: Configuration
) {
    private val logger = Logger.getLogger(this::class.java)

    fun render(templatePath: String, data: Map<String, Any> = emptyMap()): String {
        val template = fm.getTemplate(templatePath)
        val errorMap = data["errorMap"] ?: emptyMap<String, String>()
        return StringWriter().apply {
            template.process(data.plus("errorMap" to errorMap), this)
        }.toString()
    }
}
