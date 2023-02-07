package fi.metatavu.muisti.keycloak

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.cache.CacheInvalidate
import io.quarkus.cache.CacheKey
import io.quarkus.cache.CacheResult
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.Logger
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.core.Response

/**
 * Controller for Keycloak related operations
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class KeycloakController {

    @Inject
    lateinit var logger: Logger

    /**
     * Returns Keycloak client id
     */
    @ConfigProperty(name = "muisti.keycloak.admin.clientId")
    lateinit var adminResource: String

    /**
     * Returns Keycloak api secret
     */
    @ConfigProperty(name = "muisti.keycloak.admin.secret")
    lateinit var adminSecret: String

    /**
     * Returns Keycloak admin password
     */
    @ConfigProperty(name = "muisti.keycloak.admin.password")
    lateinit var adminPassword: String

    /**
     * Returns Keycloak admin username
     */
    @ConfigProperty(name = "muisti.keycloak.admin.user")
    lateinit var adminUser: String

    /**
     * Returns Keycloak realm
     */
    @ConfigProperty(name = "muisti.keycloak.admin.realm")
    lateinit var realm: String

    /**
     * Returns Keycloak server URL
     */
    @ConfigProperty(name = "muisti.keycloak.admin.host")
    lateinit var serverUrl: String

    /**
     * Finds a Keycloak user by user id
     *
     * @param userId user id
     * @return user or null if not found
     */
    @CacheResult(cacheName = "users-cache")
    fun findUserById(@CacheKey userId: UUID?): UserRepresentation? {
        userId ?: return null

        return keycloakClient.realm(realm).users().get(userId.toString()).toRepresentation() ?: return null
    }

    /**
     * Finds a Keycloak user by user email
     *
     * @param email user email
     * @return user or null if not found
     */
    fun findUserByEmail(email: String): UserRepresentation? {
        val users = searchUsers(
            username = null,
            firstName = null,
            lastName = null,
            email = email,
            firstResult = 0,
            maxResults = 1
        )

        return users.firstOrNull()
    }

    /**
     * Creates new Keycloak user
     *
     * @param email user email
     * @param language user's language
     * @param firstName user's first name
     * @param lastName user's last name
     * @param phone user's phone number
     * @param birthYear user's birth year
     * @param realmRoles list of realm roles
     * @return created user representation or null when creation has failed
     */
    fun createUser(
        email: String,
        language: String,
        firstName: String?,
        lastName: String?,
        phone: String?,
        birthYear: Int?,
        realmRoles: List<String>
    ): UserRepresentation? {
        val usersResource = keycloakClient.realm(realm).users()
        val userRepresentation = UserRepresentation()
        userRepresentation.email = email
        userRepresentation.firstName = firstName
        userRepresentation.lastName = lastName
        userRepresentation.username = email
        userRepresentation.isEnabled = true
        userRepresentation.realmRoles = realmRoles
        userRepresentation.singleAttribute(USER_ATTRIBUTE_LANGUAGE, language)
        userRepresentation.singleAttribute(USER_ATTRIBUTE_PHONE, phone)
        userRepresentation.singleAttribute(USER_ATTRIBUTE_BIRTH_YEAR, birthYear?.toString())

        try {
            val userId = getCreatedResponseId(usersResource.create(userRepresentation))
            userId ?: return null
            return usersResource.get(userId.toString()).toRepresentation()
        } catch (e: javax.ws.rs.WebApplicationException) {
            if (logger.isErrorEnabled) {
                logger.error("Failed to create user: {}", IOUtils.toString(e.response.entity as InputStream, "UTF-8"))
            }
        }

        return null
    }

    /**
     * Updates Keycloak user
     *
     * @param userRepresentation user representation in Keycloak
     * @param language user's language
     * @param firstName user's first name
     * @param lastName user's last name
     * @param phone user's phone number
     * @param birthYear user's birth year
     * @return updated user representation
     */
    @CacheInvalidate(cacheName = "users-cache")
    fun updateUser(
        @CacheKey userId: UUID,
        userRepresentation: UserRepresentation,
        language: String,
        firstName: String?,
        lastName: String?,
        phone: String?,
        birthYear: Int?
    ): UserRepresentation {
        val usersResource = keycloakClient.realm(realm).users()
        val userResource = usersResource.get(userRepresentation.id)

        userRepresentation.firstName = firstName
        userRepresentation.lastName = lastName
        userRepresentation.singleAttribute(USER_ATTRIBUTE_LANGUAGE, language)
        userRepresentation.singleAttribute(USER_ATTRIBUTE_PHONE, phone)
        userRepresentation.singleAttribute(USER_ATTRIBUTE_BIRTH_YEAR, birthYear?.toString())

        userResource.update(userRepresentation)

        return userRepresentation
    }

    /**
     * Searches users from Keycloak
     *
     * @param username filter by Keycloak username
     * @param firstName filter by firstName
     * @param lastName filter by lastName
     * @param email filter by email
     * @param email filter by email
     */
    private fun searchUsers(
        username: String?,
        firstName: String?,
        lastName: String?,
        email: String?,
        firstResult: Int?,
        maxResults: Int?
    ): List<UserRepresentation> {
        try {
            return keycloakClient.realm(realm).users().search(
                username,
                firstName,
                lastName,
                email,
                firstResult,
                maxResults
            )
        } catch (e: javax.ws.rs.WebApplicationException) {
            if (logger.isErrorEnabled) {
                logger.error("Failed to search users: {}", IOUtils.toString(e.response.entity as InputStream, "UTF-8"))
            }
        }

        return listOf()
    }

    /**
     * Returns user's language
     *
     * @param userRepresentation Keycloak user representation
     * @return user's language
     */
    fun getUserLanguage(userRepresentation: UserRepresentation?): String {
        return getUserSingleAttribute(userRepresentation, USER_ATTRIBUTE_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    /**
     * Returns user's phone number or null if not specified
     *
     * @param userRepresentation Keycloak user representation
     * @return user's phone number or null if not specified
     */
    fun getUserPhone(userRepresentation: UserRepresentation?): String? {
        return getUserSingleAttribute(userRepresentation, USER_ATTRIBUTE_PHONE)
    }

    /**
     * Returns user's birth day or null if not specified
     *
     * @param userRepresentation Keycloak user representation
     * @return user's birth day or null if not specified
     */
    fun getUserBirthYear(userRepresentation: UserRepresentation?): Int? {
        return getUserSingleAttribute(userRepresentation, USER_ATTRIBUTE_BIRTH_YEAR)?.toInt()
    }

    /**
     * Finds an id from Keycloak create response
     *
     * @param response response object
     * @return id
     */
    private fun getCreatedResponseId(response: Response): UUID? {
        if (response.status != 201) {
            try {
                if (logger.isErrorEnabled) {
                    logger.error(
                        "Failed to execute create: {}",
                        IOUtils.toString(response.entity as InputStream, "UTF-8")
                    )
                }
            } catch (e: IOException) {
                logger.error("Failed to extract error message", e)
            }
            return null
        }

        val location: String = response.getHeaderString("location")
        if (StringUtils.isBlank(location)) {
            val objectMapper = ObjectMapper()
            try {
                val idExtract: IdExtract = objectMapper.readValue(response.entity as InputStream, IdExtract::class.java)
                return UUID.fromString(idExtract.id)
            } catch (e: IOException) {
                // Ignore JSON errors
            }

            return null
        }
        val pattern: Pattern = Pattern.compile(".*/(.*)$")
        val matcher: Matcher = pattern.matcher(location)
        return if (matcher.find()) {
            UUID.fromString(matcher.group(1))
        } else null
    }

    /**
     * Returns single attribute value from Keycloak representation
     *
     * @param userRepresentation Keycloak user representation
     * @param key attribute key
     * @return user attribute value
     */
    private fun getUserSingleAttribute(userRepresentation: UserRepresentation?, key: String): String? {
        val attributes = userRepresentation?.attributes ?: return null
        return attributes[key]?.first() ?: return null
    }

    /**
     * Returns initialized Keycloak admin client
     */
    private val keycloakClient: Keycloak
        get() {
            return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(adminResource)
                .clientSecret(adminSecret)
                .grantType(OAuth2Constants.PASSWORD)
                .username(adminUser)
                .password(adminPassword)
                .authorization("Bearer $adminAccessToken")
                .build()
        }

    /**
     * Returns API admin access token
     */
    private val adminAccessToken: String?
        get() {
            return KeycloakControllerToken.getAccessToken(

            )?.accessToken
        }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class IdExtract {
        var id: String? = null
    }

    companion object {
        const val DEFAULT_LANGUAGE = "fi"
        const val USER_ATTRIBUTE_LANGUAGE = "language"
        const val USER_ATTRIBUTE_PHONE = "phone"
        const val USER_ATTRIBUTE_BIRTH_YEAR = "birth-year"
    }
}