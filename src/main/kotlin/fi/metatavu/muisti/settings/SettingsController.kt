package fi.metatavu.muisti.settings

import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import java.time.Duration
import java.time.OffsetDateTime
import java.util.Optional
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for application settings
 */
@ApplicationScoped
class SettingsController {

    @Inject
    lateinit var logger: Logger

    @ConfigProperty(name = "visitor.session.timeout")
    private lateinit var visitorSessionTimeout: Optional<String>

    /**
     * Returns time after created visitor sessions are valid
     *
     * @return time after created visitor sessions are valid
     */
    fun getVisitorSessionValidAfter(): OffsetDateTime {
        return OffsetDateTime.now().minus(getVisitorSessionTimeout())
    }

    /**
     * Returns visitor session timeout
     *
     * @return visitor session timeout
     */
    fun getVisitorSessionTimeout(): Duration {
        if (visitorSessionTimeout.isEmpty) {
            return DEFAULT_VISITOR_SESSION_TIMEOUT
        }
        var result: Duration? = null

        try {
            result = Duration.parse(visitorSessionTimeout.get())
        } catch (e: Exception) {
            logger.error("Failed to parse VISITOR_SESSION_TIMEOUT, using default value")
        }

        result ?: return DEFAULT_VISITOR_SESSION_TIMEOUT

        return result
    }

    companion object {
        val DEFAULT_VISITOR_SESSION_TIMEOUT: Duration = Duration.ofHours(6)
    }

}
