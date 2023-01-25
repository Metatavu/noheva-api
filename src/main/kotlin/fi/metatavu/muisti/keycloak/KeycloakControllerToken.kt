package fi.metatavu.muisti.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.io.IOUtils
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.eclipse.microprofile.config.ConfigProvider
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
            val serverUrl = ConfigProvider.getConfig().getValue("quarkus.oidc.auth-server-url", String::class.java)
            val realm = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.realm", String::class.java)
            val adminUser = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.user", String::class.java)
            val adminPassword = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.password", String::class.java)
            val clientId = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.clientId", String::class.java)
            val secret = ConfigProvider.getConfig().getValue("muisti.keycloak.admin.secret", String::class.java)
            logger.info("Obtaining new admin access token...")

            val uri = "$serverUrl/realms/$realm/protocol/openid-connect/token"
            try {
                HttpClients.createDefault().use { client ->
                    val httpPost = HttpPost(uri)
                    val params: MutableList<NameValuePair> = ArrayList()
                    params.add(BasicNameValuePair("client_id", clientId))
                    params.add(BasicNameValuePair("grant_type", "password"))
                    params.add(BasicNameValuePair("username", adminUser))
                    params.add(BasicNameValuePair("password", adminPassword))
                    params.add(BasicNameValuePair("client_secret", secret))
                    httpPost.entity = UrlEncodedFormEntity(params)
                    client.execute(httpPost).use { response ->
                        if (response.statusLine.statusCode != 200) {
                            logger.error("Failed obtain access token: {}", IOUtils.toString(response.entity.content, "UTF-8"))
                            return null
                        }

                        response.entity.content.use { inputStream ->
                            return jacksonObjectMapper().registerModule(JavaTimeModule()).readValue(inputStream, KeycloakAccessToken::class.java)
                        }
                    }
                }
            } catch (e: IOException) {
                logger.debug("Failed to retrieve access token", e)
            }

            return null
        }

    }

}