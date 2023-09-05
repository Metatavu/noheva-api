package fi.metatavu.noheva.api.test.functional.builder

import fi.metatavu.jaxrs.test.functional.builder.AbstractAccessTokenTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.NullAccessTokenProvider
import fi.metatavu.noheva.api.client.infrastructure.ApiClient
import fi.metatavu.noheva.api.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.noheva.api.test.functional.mqtt.TestMqttClient
import org.eclipse.microprofile.config.ConfigProvider
import java.io.IOException

/**
 * TestBuilder implementation
 *
 * @author Antti Leppä
 */
class TestBuilder(private val config: Map<String, String>) : AbstractAccessTokenTestBuilder<ApiClient>() {
    var admin = getAuthenticatedAdmin()
    var mqtt = TestMqttClient()

    /**
     * Returns client with device key auth
     *
     * @param deviceKey device key
     * @return device authorized client
     */
    fun getDevice(deviceKey: String?): TestBuilderAuthentication {
        return TestBuilderAuthentication(this, NullAccessTokenProvider(), deviceKey)
    }

    /**
     * Returns admin authenticated authentication resource
     *
     * @return admin authenticated authentication resource
     * @throws IOException
     */
    @kotlin.jvm.Throws(IOException::class)
    private fun getAuthenticatedAdmin(): TestBuilderAuthentication {
        val authServerUrl: String = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.host", String::class.java)
        val realm: String = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.realm", String::class.java)
        val clientId = ConfigProvider.getConfig().getValue("quarkus.oidc.client-id", String::class.java)
        val username = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.user", String::class.java)
        val password = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.password", String::class.java)
        return TestBuilderAuthentication(
            this,
            KeycloakAccessTokenProvider(authServerUrl, realm, clientId, username, password, null),
            deviceKey = null
        )
    }

    override fun createTestBuilderAuthentication(
        abstractTestBuilder: AbstractTestBuilder<ApiClient, AccessTokenProvider>,
        authProvider: AccessTokenProvider
    ): AuthorizedTestBuilderAuthentication<ApiClient, AccessTokenProvider> {
        return TestBuilderAuthentication(
            testBuilder = this,
            accessTokenProvider = authProvider,
            deviceKey = null
        )
    }

    override fun close() {
        mqtt.close()
        super.close()
    }
}