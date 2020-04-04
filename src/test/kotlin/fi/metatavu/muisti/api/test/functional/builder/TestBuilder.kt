package fi.metatavu.muisti.api.test.functional

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.test.functional.auth.TestBuilderAuthentication
import fi.metatavu.muisti.api.test.functional.mqtt.TestMqttClient
import fi.metatavu.muisti.api.test.functional.settings.TestSettings
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * TestBuilder implementation
 *
 * @author Antti Leppä
 */
class TestBuilder: AbstractTestBuilder<ApiClient> () {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var admin: TestBuilderAuthentication? = null

    private var mqtt: TestMqttClient? = null

    override fun createTestBuilderAuthentication(testBuilder: AbstractTestBuilder<ApiClient>, accessTokenProvider: AccessTokenProvider): AuthorizedTestBuilderAuthentication<ApiClient> {
        return TestBuilderAuthentication(testBuilder, accessTokenProvider)
    }

    /**
     * Returns admin authenticated authentication resource
     *
     * @return admin authenticated authentication resource
     * @throws IOException
     */
    @kotlin.jvm.Throws(IOException::class)
    fun admin(): TestBuilderAuthentication {
        if (admin == null) {
            val authServerUrl = TestSettings.keycloakHost
            val realm = TestSettings.keycloakRealm
            val clientId = TestSettings.keycloakClientId
            val adminUser = TestSettings.keycloakAdminUser
            val adminPassword = TestSettings.keycloakAdminPass
            val clientSecret = TestSettings.keycloakClientSecret

            admin = TestBuilderAuthentication(this, KeycloakAccessTokenProvider(authServerUrl, realm, clientId, adminUser, adminPassword, clientSecret))
        }

        return admin!!
    }

    /**
     * Returns initialized test MQTT client
     *
     * @return initialized test MQTT client
     */
    fun mqtt(): TestMqttClient {
        if (mqtt == null) {
            mqtt = TestMqttClient()
        }

        return mqtt!!
    }

    override fun close() {
        mqtt?.close()
        mqtt = null
        super.close()
    }

}