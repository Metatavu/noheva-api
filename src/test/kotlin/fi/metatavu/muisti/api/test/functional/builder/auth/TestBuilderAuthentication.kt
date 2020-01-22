package fi.metatavu.muisti.api.test.functional.auth

import java.io.IOException

import fi.metatavu.muisti.api.test.functional.impl.ExhibitionsTestBuilderResource
import fi.metatavu.muisti.api.client.infrastructure.ApiClient

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication

import fi.metatavu.muisti.api.test.functional.settings.TestSettings

/**
 * Test builder authentication
 *
 * @author Antti Leppä
 */
class TestBuilderAuthentication: AuthorizedTestBuilderAuthentication<ApiClient> {

  private val exhibitions: ExhibitionsTestBuilderResource? = null

  constructor(testBuilder: AbstractTestBuilder<ApiClient>, accessTokenProvider: AccessTokenProvider) : super(testBuilder, accessTokenProvider)

  /**
   * Returns test builder resource for exhibitions
   *
   * @return test builder resource for exhibitions
   * @throws IOException thrown when authentication fails
   */
  @kotlin.jvm.Throws(IOException::class)
  fun exhibitions(): ExhibitionsTestBuilderResource? {
    return if (exhibitions != null) {
      exhibitions
    } else ExhibitionsTestBuilderResource(getTestBuilder(), createClient())
  }

  override fun createClient(accessToken: String): ApiClient {
    val result = ApiClient(TestSettings.apiBasePath);
    return result
  }

}