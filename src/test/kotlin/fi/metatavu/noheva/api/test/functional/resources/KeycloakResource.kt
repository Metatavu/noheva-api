package fi.metatavu.noheva.api.test.functional.resources

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
        config["quarkus.oidc.auth-server-url"] = "${keycloak.authServerUrl}realms/$realm"
        config["quarkus.oidc.client-id"] = "test"
        config["muisti.keycloak.admin.host"] = keycloak.authServerUrl
        config["muisti.keycloak.admin.realm"] = realm

        config["muisti.keycloak.admin.clientId"] = "admin"
        config["muisti.keycloak.admin.secret"] = "15114155-3693-4b80-85c5-f39cb4d02e5f"

        config["muisti.keycloak.admin.user"] = "api"
        config["muisti.keycloak.admin.password"] = "d0abd401-7781-46f0-8146-643f5f946256"
        return config
    }

    override fun stop() {
        keycloak.stop()
    }

    companion object {
        val keycloak: KeycloakContainer = KeycloakContainer()
            .withRealmImportFile("kc.json")
    }
}