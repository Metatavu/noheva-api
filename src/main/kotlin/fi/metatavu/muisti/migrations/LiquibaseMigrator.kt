package fi.metatavu.muisti.migrations

import io.quarkus.runtime.StartupEvent
import io.quarkus.runtime.util.ExceptionUtil
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.exception.LiquibaseException
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.enterprise.event.Observes

/**
 * Custom liquibase migrator class
 *
 * @author Jari Nykänen
 */
class LiquibaseMigrator {

    @ConfigProperty(name = "custom.liquibase.migrate")
    var runMigration = false

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    lateinit var datasourceUrl: String

    @ConfigProperty(name = "quarkus.datasource.username")
    lateinit var datasourceUsername: String

    @ConfigProperty(name = "quarkus.datasource.password")
    lateinit var datasourcePassword: String

    @ConfigProperty(name = "quarkus.liquibase.change-log")
    lateinit var changeLogLocation: String

    /**
     * Runs liquibase migration
     *
     * @throws LiquibaseException liquibase exception
     */
    @Throws(LiquibaseException::class)
    fun runLiquibaseMigration(@Observes event: StartupEvent?) {
        if (runMigration) {
            var liquibase: Liquibase? = null
            try {
                val resourceAccessor: ResourceAccessor =
                    ClassLoaderResourceAccessor(Thread.currentThread().contextClassLoader)
                val conn = DatabaseFactory.getInstance()
                    .openConnection(
                        datasourceUrl,
                        datasourceUsername,
                        datasourcePassword,
                        null,
                        resourceAccessor
                    )
                liquibase = Liquibase(changeLogLocation, resourceAccessor, conn)
                liquibase.update(Contexts(), LabelExpression())
            } catch (e: Exception) {
                println("Liquibase Migration Exception: " + ExceptionUtil.generateStackTrace(e))
            } finally {
                liquibase?.close()
            }
        }
    }
}