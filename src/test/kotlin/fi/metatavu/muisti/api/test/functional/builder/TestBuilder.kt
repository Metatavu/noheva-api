package fi.metatavu.muisti.api.test.functional.builder

import fi.metatavu.jaxrs.test.functional.builder.AbstractAccessTokenTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.muisti.api.test.functional.mqtt.TestMqttClient
import fi.metatavu.muisti.api.test.functional.settings.MqttTestSettings
import org.eclipse.microprofile.config.ConfigProvider
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * TestBuilder implementation
 *
 * @author Antti Leppä
 */
class TestBuilder(private val config: Map<String, String>) : AbstractAccessTokenTestBuilder<ApiClient>() {

    private var admin: TestBuilderAuthentication? = null

    private var mqtt: TestMqttClient? = null


    /**
     * Returns admin authenticated authentication resource
     *
     * @return admin authenticated authentication resource
     * @throws IOException
     */
    @kotlin.jvm.Throws(IOException::class)
    fun admin(): TestBuilderAuthentication {
        if (admin == null) {
            val authServerUrl: String =
                ConfigProvider.getConfig().getValue("muisti.keycloak.admin.host", String::class.java)
            val realm: String = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.realm", String::class.java)
            val clientId = ConfigProvider.getConfig().getValue("quarkus.oidc.client-id", String::class.java)
            val username = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.user", String::class.java)
            val password = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.password", String::class.java)
            admin = TestBuilderAuthentication(
                this,
                KeycloakAccessTokenProvider(authServerUrl, realm, clientId, username, password, null)
            )
        }

        return admin!!
    }

    override fun createTestBuilderAuthentication(
        abstractTestBuilder: AbstractTestBuilder<ApiClient, AccessTokenProvider>,
        authProvider: AccessTokenProvider
    ): AuthorizedTestBuilderAuthentication<ApiClient, AccessTokenProvider> {
        return TestBuilderAuthentication(this, authProvider)
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

    //todo is it proper place mqtt client
}