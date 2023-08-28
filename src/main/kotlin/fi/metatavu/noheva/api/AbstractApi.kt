package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.model.Error
import fi.metatavu.noheva.api.spec.model.ScreenOrientation
import fi.metatavu.noheva.crypto.CryptoController
import fi.metatavu.noheva.devices.DeviceController
import org.apache.commons.lang3.EnumUtils
import org.apache.commons.lang3.StringUtils
import org.eclipse.microprofile.jwt.JsonWebToken
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Context
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

/**
 * Abstract base class for all API services
 *
 * @author Antti Leppä
 */
@RequestScoped
abstract class AbstractApi {

    @Inject
    lateinit var jsonWebToken: JsonWebToken

    @Inject
    protected lateinit var cryptoController: CryptoController

    @Inject
    protected lateinit var deviceController: DeviceController

    @Context
    private lateinit var securityContext: SecurityContext

    @Context
    lateinit var httpHeaders: HttpHeaders

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
     * Returns logged user id
     *
     * @return logged user id
     */
    protected val loggedUserId: UUID?
        get() {
            if (jsonWebToken.subject != null) {
                return UUID.fromString(jsonWebToken.subject)
            }

            return null
        }

    /**
     * Checks whether incoming request from Device has authorized device key as a header
     *
     * @param deviceId device id
     * @return whether device key is authorized
     */
    protected fun isAuthorizedDevice(deviceId: UUID): Boolean {
        val deviceKeyHeader = httpHeaders.requestHeaders[DEVICE_KEY_HEADER]
        if (deviceKeyHeader.isNullOrEmpty()) {
            return false
        }
        val token = deviceKeyHeader.firstOrNull() ?: return false
        val privateKey = cryptoController.loadPrivateKeyBase64(token) ?: return false
        val deviceKey = deviceController.getDeviceKey(deviceId) ?: return false
        val publicKey = cryptoController.loadPublicKey(deviceKey) ?: return false
        val challenge = cryptoController.signUUID(
            privateKey = privateKey,
            id = deviceId
        ) ?: return false

        return cryptoController.verifyUUID(
            publicKey = publicKey,
            signature = challenge,
            id = deviceId
        )
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
     * Constructs created response
     *
     * @param entity payload
     * @return response
     */
    protected fun createCreated(entity: Any?): Response {
        return Response
            .status(Response.Status.CREATED)
            .entity(entity)
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
        val entity = Error(
            message = message,
            code = status.statusCode
        )

        return Response
            .status(status)
            .entity(entity)
            .build()
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

    companion object {
        const val NOT_FOUND_MESSAGE = "Not found"
        const val UNAUTHORIZED = "Unauthorized"
        const val FORBIDDEN = "Forbidden"
        const val EXHIBITION_NOT_FOUND = "Exhibition not found"
        const val MISSING_REQUEST_BODY = "Missing request body"
        const val VISITOR_NOT_FOUND = "Visitor not found"
        const val VISITOR_SESSION_NOT_FOUND = "Visitor session not found"
        const val RFID_ANTENNA_NOT_FOUND = "RFID antenna not found"
        const val CONTENT_VERSION_NOT_FOUND = "Content version not found"
        const val GROUP_CONTENT_VERSION_NOT_FOUND = "Group content version not found"
        const val STORED_FILE_NOT_FOUND = "Stored file not found"
        const val VISITOR_VARIABLE_NOT_FOUND = "Visitor variable not found"
        const val DEVICE_MODEL_NOT_FOUND = "Device model not found"
        const val INVALID_LAYOUT_TYPE = "Data does not match layout type"

        const val DEVICE_KEY_HEADER = "X-DEVICE-KEY"
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