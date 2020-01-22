package fi.metatavu.muisti.liquibase

import liquibase.integration.cdi.CDILiquibaseConfig
import liquibase.integration.cdi.annotations.LiquibaseType
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import org.apache.commons.lang3.StringUtils
import java.sql.SQLException
import java.util.*
import javax.annotation.Resource
import javax.enterprise.context.Dependent
import javax.enterprise.inject.Produces
import javax.sql.DataSource
import kotlin.collections.ArrayList

/**
 * Liquibase producer
 *
 * @author Antti Leppä
 */
@Dependent
class LiquibaseProducer {

    @Resource(lookup = "java:jboss/datasources/muisti-api")
    private val dataSource: DataSource? = null

    /**
     * Creates Liquibase config
     *
     * @return Liquibase config
     */
    @Produces
    @LiquibaseType
    fun createConfig(): CDILiquibaseConfig {
        val contextList: MutableList<String?> = ArrayList()
        if ("TEST" == System.getProperty("runmode")) {
            contextList.add("test")
        } else {
            contextList.add("production")
        }
        val contexts = StringUtils.join(contextList, ',')
        val config = CDILiquibaseConfig()
        config.setChangeLog("fi/metatavu/muisti/changelog.xml")
        config.setContexts(contexts)
        return config
    }

    /**
     * Creates Liquibase data source
     *
     * @return Liquibase data source
     * @throws SQLException
     */
    @Produces
    @LiquibaseType
    @Throws(SQLException::class)
    fun createDataSource(): DataSource? {
        return dataSource
    }

    /**
     * Creates resource accessor for Liquibase
     *
     * @return resource accessor
     */
    @Produces
    @LiquibaseType
    fun create(): ResourceAccessor {
        return ClassLoaderResourceAccessor(javaClass.classLoader)
    }
}