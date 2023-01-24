package fi.metatavu.muisti.api.test.functional.resources

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import kotlin.collections.HashMap

/**
 * Starts test container for Keycloak
 */
class KeycloakResource : QuarkusTestResourceLifecycleManager {
    override fun start(): Map<String, String> {
        keycloak.start()
        val realm = "muisti"
        val config: MutableMap<String, String> = HashMap()
        config["quarkus.oidc.auth-server-url"] = "${keycloak.authServerUrl}/realms/$realm"
        config["quarkus.oidc.client-id"] = "test"
        config["muisti.keycloak.admin.host"] = keycloak.authServerUrl
        config["muisti.keycloak.admin.realm"] = realm

        config["muisti.keycloak.admin.user"] = "admin"
        config["muisti.keycloak.admin.password"] = "test"
        return config
    }

    override fun stop() {
        keycloak.stop()
    }

    companion object {
        const val serverAdminUser = "admin"
        const val serverAdminPass = "admin"
        val keycloak: KeycloakContainer = KeycloakContainer()
            .withAdminUsername(serverAdminUser)
            .withAdminPassword(serverAdminPass)
            .withRealmImportFile("kc.json")
    }
}