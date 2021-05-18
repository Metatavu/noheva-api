package fi.metatavu.muisti.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.commons.io.IOUtils
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.OffsetDateTime
import java.util.ArrayList

/**
 * Class for maintaining the keycloak controller access token
 */
class KeycloakControllerToken {

    companion object {

        private val logger = LoggerFactory.getLogger(KeycloakControllerToken::class.java.name)

        private const val expireSlack = 60L

        private var accessToken: KeycloakAccessToken? = null

        private var accessTokenExpires: OffsetDateTime? = null

        /**
         * Resolves a admin access token from Keycloak
         *
         * @return access token
         */
        fun getAccessToken(): KeycloakAccessToken? {
            try {
                synchronized(this) {
                    val now = OffsetDateTime.now()
                    val expires = accessTokenExpires?.minusSeconds(expireSlack)

                    if ((accessToken == null) || expires == null || expires.isBefore(now)) {
                        accessToken = obtainAccessToken()
                        if (accessToken == null) {
                            logger.error("Could not obtain access token")
                            return null
                        }

                        val expiresIn = accessToken?.expiresIn
                        if (expiresIn == null) {
                            logger.error("Could not resolve access token expires in")
                            return null
                        }

                        accessTokenExpires = OffsetDateTime.now().plusSeconds(expiresIn)
                    }

                    return accessToken
                }
            } catch (e: Exception) {
                logger.error("Failed to retrieve access token", e)
            }

            return null
        }

        /**
         * Obtains fresh admin access token from Keycloak
         */
        private fun obtainAccessToken(): KeycloakAccessToken? {
            logger.info("Obtaining new admin access token...")

            val uri = "$serverUrl/realms/$realm/protocol/openid-connect/token"
            try {
                HttpClients.createDefault().use { client ->
                    val httpPost = HttpPost(uri)
                    val params: MutableList<NameValuePair> = ArrayList()
                    params.add(BasicNameValuePair("client_id", adminResource))
                    params.add(BasicNameValuePair("grant_type", "password"))
                    params.add(BasicNameValuePair("username", adminUser))
                    params.add(BasicNameValuePair("password", adminPassword))
                    params.add(BasicNameValuePair("client_secret", adminSecret))
                    httpPost.entity = UrlEncodedFormEntity(params)
                    client.execute(httpPost).use { response ->
                        if (response.statusLine.statusCode != 200) {
                            logger.error("Failed obtain access token: {}", IOUtils.toString(response.entity.content, "UTF-8"))
                            return null
                        }

                        response.entity.content.use { inputStream ->
                            val objectMapper = ObjectMapper()
                            objectMapper.registerModule(JavaTimeModule())
                            objectMapper.registerModule(KotlinModule())
                            return objectMapper.readValue(inputStream, KeycloakAccessToken::class.java)
                        }
                    }
                }
            } catch (e: IOException) {
                logger.debug("Failed to retrieve access token", e)
            }

            return null
        }

        /**
         * Returns Keycloak client id
         */
        private val adminResource: String
            get() = System.getenv("KEYCLOAK_ADMIN_RESOURCE")

        /**
         * Returns Keycloak api secret
         */
        private val adminSecret: String
            get() = System.getenv("KEYCLOAK_ADMIN_SECRET")

        /**
         * Returns Keycloak admin password
         */
        private val adminPassword: String
            get() = System.getenv("KEYCLOAK_ADMIN_PASSWORD")

        /**
         * Returns Keycloak admin username
         */
        private val adminUser: String
            get() = System.getenv("KEYCLOAK_ADMIN_USERNAME")

        /**
         * Returns Keycloak realm
         */
        private val realm: String
            get() = System.getenv("KEYCLOAK_REALM")

        /**
         * Returns Keycloak server URL
         */
        private val serverUrl: String
            get() = System.getenv("KEYCLOAK_URL")
    }

}