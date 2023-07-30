import org.jooq.meta.jaxb.Strategy
import java.sql.DriverManager
import java.util.Properties

plugins {
    java
    id("io.quarkus")
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.allopen") version "1.8.21"
}

buildscript {
    dependencies {
        classpath("org.testcontainers:testcontainers:1.18.3")
        classpath("org.testcontainers:postgresql:1.18.3")
        classpath("org.liquibase:liquibase-core:4.22.0")
        classpath("org.jooq:jooq:3.18.4")
        classpath("org.jooq:jooq-codegen:3.18.4")
        classpath("org.jooq:jooq-meta:3.18.4")
        classpath("org.postgresql:postgresql:42.6.0")
        classpath("io.quarkus:quarkus-jdbc-postgresql")
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation("io.quarkus:quarkus-agroal")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.21")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jooq:jooq:3.18.4")
    implementation("org.jboss.slf4j:slf4j-jboss-logmanager")

    implementation("org.freemarker:freemarker:2.3.31")

//    implementation("org.liquibase:liquibase-core:4.4.3")
    implementation("io.quarkus:quarkus-liquibase")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "my.starter.project"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

allOpen { 
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.quarkusDev.configure {
    workingDirectory.set(projectDir)
}

class MyJooqGeneratorStrategy: org.jooq.codegen.DefaultGeneratorStrategy() {
    override fun getJavaClassName(definition: org.jooq.meta.Definition?, mode: org.jooq.codegen.GeneratorStrategy.Mode?): String {
        val className = super.getJavaClassName(definition, mode)
        println("example customizations for : $definition")
        return if (definition == null || definition.name.isNullOrBlank()) {
            className
        } else if (definition.name.endsWith("user_genders")) {
            "UserGender"
        } else {
            className.removeSuffix("s")
        }
    }
}

abstract class JooqGenerate: DefaultTask() {
    private val dbUsername = "postgres"
    private val dbPassword = dbUsername
    private val dbName = dbUsername

    @TaskAction
    fun run() {
        val pg = createPGContainer()
        try {
            val (props, resourceDir) = readApplicationProperties()
            migrateSchema(pg, props, resourceDir)
            generateJooQSchema(pg, props)
        } finally {
            pg.stop()
        }
    }

    private fun readApplicationProperties(): Pair<Properties, java.nio.file.Path> {
        val resourcesDir = project.projectDir.toPath().resolve("src/main/resources")
        val propsFile = project.projectDir.resolve("$resourcesDir/application.properties")
        val props = propsFile.inputStream().use {fis ->
            Properties().apply {
                load(fis)
            }
        }
        return Pair(props, resourcesDir)
    }

    private fun createPGContainer(): org.testcontainers.containers.PostgreSQLContainer<Nothing> {
        return org.testcontainers.containers.PostgreSQLContainer<Nothing>("postgres:latest").apply {
            withUsername(dbUsername)
            withPassword(dbPassword)
            withDatabaseName(dbName)
            start()
            println("Started PG")
        }
    }
    private fun migrateSchema(pg: org.testcontainers.containers.PostgreSQLContainer<Nothing>, props: Properties, resourceDir: java.nio.file.Path) {
        Class.forName("org.postgresql.Driver")
        val jdbcUrl = pg.jdbcUrl
        DriverManager.getConnection(jdbcUrl, pg.username, pg.password).use { connection ->
            connection.autoCommit = true

            val db = liquibase.database.DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                liquibase.database.jvm.JdbcConnection(
                    connection
                )
            ).apply {
                defaultSchemaName = props.getProperty("quarkus.liquibase.default-schema-name")
                liquibaseSchemaName = props.getProperty("quarkus.liquibase.liquibase-schema-name")
            }
            val changeLog = resourceDir.resolve(props.getProperty("quarkus.liquibase.change-log"))
            liquibase.Liquibase(
                changeLog.fileName.toString(),
                liquibase.resource.DirectoryResourceAccessor(changeLog.parent),
                db
            ).use { lb ->
                lb.setChangeExecListener(object : liquibase.changelog.visitor.DefaultChangeExecListener() {
                    override fun ran(
                        changeSet: liquibase.changelog.ChangeSet?,
                        databaseChangeLog: liquibase.changelog.DatabaseChangeLog?,
                        database: liquibase.database.Database?,
                        execType: liquibase.changelog.ChangeSet.ExecType?
                    ) {
                        println("Executed SQL changeset ${changeSet} at ${database?.connection?.url}")
                    }
                })
                lb.update("") //https://forum.liquibase.org/t/4-21-1-is-out-deprecates-liquibase-update/8087/3
                println("Done with LiquibaseMigrate")
            }
        }
    }
    private fun generateJooQSchema(pg: org.testcontainers.containers.PostgreSQLContainer<Nothing>, props: Properties) {
        val generatedDir = project.projectDir.toPath().resolve("src").resolve("main").resolve("kotlin")
        val conf = org.jooq.meta.jaxb.Configuration()
            .withLogging(org.jooq.meta.jaxb.Logging.DEBUG)
            .withOnError(org.jooq.meta.jaxb.OnError.LOG)
            .withJdbc(
                org.jooq.meta.jaxb.Jdbc()
                    .withDriver("org.postgresql.Driver")
                    .withUrl(pg.jdbcUrl)
                    .withUsername(pg.username)
                    .withPassword(pg.password)
            ).withGenerator((org.jooq.meta.jaxb.Generator()
                .withName("org.jooq.codegen.KotlinGenerator")
                .withDatabase(
                    org.jooq.meta.jaxb.Database()
                        .withName("org.jooq.meta.postgres.PostgresDatabase")
                        .withIncludes(".*")
                        .withInputSchema(props.getProperty("quarkus.liquibase.default-schema-name"))
                        .withOutputSchemaToDefault(true)
                )
                .withGenerate(org.jooq.meta.jaxb.Generate()
                    .withPojos(true)
                    .withPojosAsKotlinDataClasses(true)
                    .withKotlinNotNullPojoAttributes(true)
                    .withKotlinNotNullRecordAttributes(true)
                    .withKotlinNotNullInterfaceAttributes(true)
                )
                .withTarget(
                    org.jooq.meta.jaxb.Target()
                        .withPackageName("my.starter.jooq")
                        .withDirectory(generatedDir.toAbsolutePath().toString())
                ).withStrategy(Strategy()
                    .withName("Build_gradle\$MyJooqGeneratorStrategy")
                )))
        org.jooq.codegen.GenerationTool.generate(conf)
        println("Code Generated at ${generatedDir}")
    }
}

tasks.register<JooqGenerate>("generateJooqClasses")
