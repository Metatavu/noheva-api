package fi.metatavu.muisti.api.test.functional.settings

import org.apache.commons.lang3.math.NumberUtils

/**
 * Utility class for retrieving functional test settings
 *
 * @author Antti Leppä
 */
object TestSettings {

    val apiBasePath: String
        get() = "http://localhost:8080/v1"

    val keycloakHost: String
        get() = "http://test-keycloak:8080/auth"

    val keycloakRealm: String
        get() = "muisti"

    val keycloakClientId: String
        get() = "test"

    val keycloakClientSecret: String?
        get() = null

    val keycloakAdminUser: String
        get() = "admin"

    val keycloakAdminPass: String
        get() = "test"

}