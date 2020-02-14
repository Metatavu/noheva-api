package fi.metatavu.muisti.api.test.functional.auth

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.test.functional.impl.ExhibitionsTestBuilderResource
import fi.metatavu.muisti.api.test.functional.impl.VisitorSessionTestBuilderResource
import fi.metatavu.muisti.api.test.functional.settings.TestSettings
import java.io.IOException

/**
 * Test builder authentication
 *
 * @author Antti Leppä
 */
class TestBuilderAuthentication
/**
 * Constructor
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 */(testBuilder: AbstractTestBuilder<ApiClient>, accessTokenProvider: AccessTokenProvider) : AuthorizedTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

  private var accessTokenProvider: AccessTokenProvider? = accessTokenProvider
  private var exhibitions: ExhibitionsTestBuilderResource? = null
  private var visitorSessions: VisitorSessionTestBuilderResource? = null

  /**
   * Returns test builder resource for exhibitions
   *
   * @return test builder resource for exhibitions
   * @throws IOException thrown when authentication fails
   */
  @kotlin.jvm.Throws(IOException::class)
  fun exhibitions(): ExhibitionsTestBuilderResource {
    if (exhibitions == null) {
      exhibitions = ExhibitionsTestBuilderResource(getTestBuilder(), this.accessTokenProvider, createClient())
    }

    return exhibitions!!
  }

  /**
   * Returns test builder resource for visitorSessions
   *
   * @return test builder resource for visitorSessions
   * @throws IOException thrown when authentication fails
   */
  @kotlin.jvm.Throws(IOException::class)
  fun visitorSessions(): VisitorSessionTestBuilderResource {
    if (visitorSessions == null) {
      visitorSessions = VisitorSessionTestBuilderResource(getTestBuilder(), this.accessTokenProvider, createClient())
    }

    return visitorSessions!!
  }

  /**
   * Creates a API client
   *
   * @param accessToken access token
   * @return API client
   */
  override fun createClient(accessToken: String): ApiClient {
    val result = ApiClient(TestSettings.apiBasePath)
    ApiClient.accessToken = accessToken
    return result
  }

}