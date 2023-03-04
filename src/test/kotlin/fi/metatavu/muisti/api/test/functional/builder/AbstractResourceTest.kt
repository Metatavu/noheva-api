package fi.metatavu.muisti.api.test.functional.builder

import io.quarkus.test.common.DevServicesContext
import io.quarkus.test.junit.QuarkusTest
import org.eclipse.microprofile.config.ConfigProvider

/**
 * Abstract base class for resource tests
 */
@QuarkusTest
abstract class AbstractResourceTest {

    private var devServicesContext: DevServicesContext? = null

    /**
     * Creates new test builder
     *
     * @return new test builder
     */
    protected fun createTestBuilder(): TestBuilder {
        return TestBuilder(getConfig())
    }

    /**
     * Returns config for tests.
     *
     * If tests are running in native mode, method returns config from devServicesContext and
     * when tests are running in JVM mode method returns config from the Quarkus config
     *
     * @return config for tests
     */
    private fun getConfig(): Map<String, String> {
        return getQuarkusConfig()
    }

    /**
     * Returns test config from Quarkus
     *
     * @return test config from Quarkus
     */
    private fun getQuarkusConfig(): Map<String, String> {
        val config = ConfigProvider.getConfig()
        return config.propertyNames.associateWith { config.getConfigValue(it).rawValue }
    }

}