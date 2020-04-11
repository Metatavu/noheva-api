package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.model.Error
import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import org.apache.commons.lang3.EnumUtils
import org.apache.commons.lang3.StringUtils
import org.jboss.resteasy.spi.ResteasyProviderFactory
import org.keycloak.KeycloakPrincipal
import org.keycloak.KeycloakSecurityContext
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.authorization.client.ClientAuthorizationContext
import org.keycloak.representations.AccessToken
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.Response

/**
 * Abstract base class for all API services
 *
 * @author Antti Leppä
 */
abstract class AbstractApi {

    protected val EXHIBITION_NOT_FOUND = "Exhibition not found"
    protected val UNAUTHORIZED = "Unauthorized"

    /**
     * Returns list parameter as <E> translated by given translate function.
     *
     * @param parameter parameter as string list
     * @param translate translate function
     * @return list of <E>
     */
    protected fun <E> getListParameter(parameter: List<String?>?, translate: Function<String, E>): List<E>? {
        if (parameter == null) {
            return null
        }
        val merged: MutableList<String> = ArrayList()
        parameter.stream()
                .filter { css: String? -> StringUtils.isNoneEmpty(css) }
                .forEach { filter: String? -> merged.addAll(Arrays.asList(*StringUtils.split(filter, ','))) }
        return merged.stream()
                .map { t: String -> translate.apply(t) }
                .collect(Collectors.toList())
    }

    /**
     * Returns list parameter as <E> translated by given translate function.
     *
     * @param parameter list parameter as string
     * @param translate translate function
     * @return list of <E>
     */
    protected fun <E> getListParameter(parameter: String?, translate: Function<String, E>): List<E>? {
        return if (parameter == null) {
            null
        } else getListParameter(Arrays.asList(*StringUtils.split(parameter, ',')), translate)
    }

    /**
     * Parses CSV enum parameter from string list into enum list
     *
     * @param enumType target enum class
     * @param parameter string values
     * @return list of enums
     * @throws IllegalArgumentException if parameters contain invalid values
     */
    protected fun <T : Enum<T?>?> getEnumListParameter(enumType: Class<T>, parameter: List<String>?): List<T>? {
        return getListParameter(parameter, Function { name: String -> java.lang.Enum.valueOf(enumType, name) })
    }

    /**
     * Translates enum to other enum
     *
     * @param targetClass target enum
     * @param original original enum
     * @return translated enum
     */
    protected fun <E : Enum<E>?> translateEnum(targetClass: Class<E>?, original: Enum<*>?): E? {
        return if (original == null) {
            null
        } else EnumUtils.getEnum(targetClass, original.name)
    }

    /**
     * Return current HttpServletRequest
     *
     * @return current http servlet request
     */
    protected val httpServletRequest: HttpServletRequest
        get() = ResteasyProviderFactory.getContextData(HttpServletRequest::class.java)

    /**
     * Returns logged user id
     *
     * @return logged user id
     */
    protected val loggerUserId: UUID?
        get() {
            val httpServletRequest = httpServletRequest
            val remoteUser = httpServletRequest.remoteUser ?: return null
            return UUID.fromString(remoteUser)
        }

    /**
     * Constructs ok response
     *
     * @param entity payload
     * @return response
     */
    protected fun createOk(entity: Any?): Response {
        return Response
                .status(Response.Status.OK)
                .entity(entity)
                .build()
    }

    /**
     * Constructs ok response
     *
     * @param entity payload
     * @param totalHits total hits
     * @return response
     */
    protected fun createOk(entity: Any?, totalHits: Long?): Response {
        return Response
                .status(Response.Status.OK)
                .entity(entity)
                .header("Total-Results", totalHits)
                .build()
    }

    /**
     * Constructs no content response
     *
     * @param entity payload
     * @return response
     */
    protected fun createAccepted(entity: Any?): Response {
        return Response
                .status(Response.Status.ACCEPTED)
                .entity(entity)
                .build()
    }

    /**
     * Constructs no content response
     *
     * @return response
     */
    protected fun createNoContent(): Response {
        return Response
                .status(Response.Status.NO_CONTENT)
                .build()
    }

    /**
     * Constructs bad request response
     *
     * @param message message
     * @return response
     */
    protected fun createBadRequest(message: String): Response {
        return createError(Response.Status.BAD_REQUEST, message)
    }

    /**
     * Constructs not found response
     *
     * @param message message
     * @return response
     */
    protected fun createNotFound(message: String): Response {
        return createError(Response.Status.NOT_FOUND, message)
    }

    /**
     * Constructs not found response
     *
     * @param message message
     * @return response
     */
    protected fun createConflict(message: String): Response {
        return createError(Response.Status.CONFLICT, message)
    }

    /**
     * Constructs not implemented response
     *
     * @param message message
     * @return response
     */
    protected fun createNotImplemented(message: String): Response {
        return createError(Response.Status.NOT_IMPLEMENTED, message)
    }

    /**
     * Constructs internal server error response
     *
     * @param message message
     * @return response
     */
    protected fun createInternalServerError(message: String): Response {
        return createError(Response.Status.INTERNAL_SERVER_ERROR, message)
    }

    /**
     * Constructs forbidden response
     *
     * @param message message
     * @return response
     */
    protected fun createForbidden(message: String): Response {
        return createError(Response.Status.FORBIDDEN, message)
    }

    /**
     * Constructs unauthorized response
     *
     * @param message message
     * @return response
     */
    protected fun createUnauthorized(message: String): Response {
        return createError(Response.Status.UNAUTHORIZED, message)
    }

    /**
     * Constructs an error response
     *
     * @param status status code
     * @param message message
     *
     * @return error response
     */
    private fun createError(status: Response.Status, message: String): Response {
        val entity = Error()

        entity.message = message
        entity.code = status.statusCode

        return Response
                .status(status)
                .entity(entity)
                .build()
    }

    /**
     * Returns whether logged user has at least one of specified organization roles
     *
     * @param roles roles
     * @return whether logged user has specified organization role or not
     */
    protected fun hasOrganizationRole(vararg roles: String?): Boolean {
        val keycloakSecurityContext = keycloakSecurityContext ?: return false
        val token = keycloakSecurityContext.token ?: return false
        val realmAccess = token.realmAccess ?: return false
        for (i in 0 until roles.size) {
            if (realmAccess.isUserInRole(roles[i])) {
                return true
            }
        }
        return false
    }

    /**
     * Return keycloak authorization client
     */
    protected val authzClient: AuthzClient?
        get() {
            val clientAuthorizationContext: ClientAuthorizationContext = authorizationContext ?: return null
            return clientAuthorizationContext.getClient()
        }

    /**
     * Parses date time from string
     *
     * @param timeString
     * @return
     */
    protected fun parseTime(timeString: String?): OffsetDateTime? {
        return if (StringUtils.isEmpty(timeString)) {
            null
        } else OffsetDateTime.parse(timeString)
    }

    /**
     * Parses CDT list into set of UUIDs.
     *
     * @param cdt CDT list
     * @return set of UUID values
     */
    protected fun parseUuidCDT(cdt: String?): Set<UUID> {
        return if (cdt == null) {
            emptySet()
        } else Arrays.stream(StringUtils.split(cdt, ','))
                .map { name: String? -> UUID.fromString(name) }
                .filter { obj: UUID? -> Objects.nonNull(obj) }
                .collect(Collectors.toSet())
    }

    /**
     * Returns keycloak security context from request or null if not available
     */
    private val keycloakSecurityContext: KeycloakSecurityContext?
        get() {
            val request = httpServletRequest
            val userPrincipal = request.userPrincipal
            val kcPrincipal = userPrincipal as KeycloakPrincipal<*>
            return kcPrincipal.keycloakSecurityContext
        }

    /**
     * Return keycloak authorization client context or null if not available
     */
    private val authorizationContext: ClientAuthorizationContext?
        get() {
            val keycloakSecurityContext = keycloakSecurityContext ?: return null
            return keycloakSecurityContext.authorizationContext as ClientAuthorizationContext
        }

    /**
     * Returns access token
     *
     * @return access token
     */
    protected val accessToken: AccessToken?
        get() {
            val keycloakSecurityContext = keycloakSecurityContext ?: return null
            return keycloakSecurityContext.token
        }

    companion object {
        protected const val NOT_FOUND_MESSAGE = "Not found"
        protected const val UNAUTHORIZED = "Unauthorized"
        protected const val FORBIDDEN = "Forbidden"
    }

    /**
     * Convert string to screen orientation
     *
     * @param orientation screen orientation string
     * @return ScreenOrientation or null if sting cannot be converted
     */
    protected fun convertStringToScreenOrientation(orientation: String): ScreenOrientation? {
        for (b in ScreenOrientation.values()) {
            if (b.toString() == orientation) {
                return b
            }
        }

        return null
    }
}