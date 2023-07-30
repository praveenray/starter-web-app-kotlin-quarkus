package my.starter.configs

import com.fasterxml.jackson.databind.ObjectMapper
import freemarker.cache.ConditionalTemplateConfigurationFactory
import freemarker.cache.FileExtensionMatcher
import freemarker.cache.FirstMatchTemplateConfigurationFactory
import freemarker.cache.NullCacheStorage
import freemarker.core.HTMLOutputFormat
import freemarker.core.TemplateConfiguration
import freemarker.template.Configuration
import freemarker.template.SimpleHash
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import my.starter.libs.Utils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import javax.sql.DataSource

class Configurations(
    private val datasource: DataSource,
    private val utils: Utils,
    private val jackson: ObjectMapper,
    @ConfigProperty(name="is-development") private val isDev: Boolean,
    @ConfigProperty(name="release.version") private val releaseVersion: String
) {
    companion object {
        val logger = Logger.getLogger(this::class.java)
        val APP_RELEASE_VERSION = "APP_RELEASE_VERSION"
        val UTILS = "utils"
        val JACKSON = "jackson"
    }

    @Produces
    @ApplicationScoped
    fun jooqBean(): DSLContext {
        val configs = DefaultConfiguration()
            .set(datasource)
            .set(SQLDialect.POSTGRES)
            .set(
                Settings()
                    .withExecuteLogging(true)
                    .withRenderFormatted(true)
                    .withRenderCatalog(false)
                    .withRenderSchema(false)
                    .withMaxRows(Int.MAX_VALUE)
                    .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_QUOTED)
                    .withRenderNameCase(RenderNameCase.LOWER_IF_UNQUOTED)
            )
        return DSL.using(configs)
    }

    @Produces
    @ApplicationScoped
    fun configureFreemarker(): Configuration {
        return Configuration(Configuration.VERSION_2_3_31).apply {
            setAutoImports(mapOf(
                "layout" to "/layout/index.ftl"
            ))
            setAllSharedVariables(
                SimpleHash(
                mapOf(
                    APP_RELEASE_VERSION to releaseVersion,
                    UTILS to utils,
                    JACKSON to jackson,
                ),
                objectWrapper
            ))
            templateConfigurations = FirstMatchTemplateConfigurationFactory(
                ConditionalTemplateConfigurationFactory(FileExtensionMatcher("ftl"), TemplateConfiguration().apply { outputFormat = HTMLOutputFormat.INSTANCE })
            )
            if (isDev) {
                logger.info("CONFIGURING Freemarker FOR DEV")
                cacheStorage = NullCacheStorage()
                templateUpdateDelayMilliseconds = 0
                setDirectoryForTemplateLoading(utils.workingFolder().resolve("src/main/resources/static").toFile())
            } else {
                setClassForTemplateLoading(this::class.java, "/static")
            }
        }
    }
}
