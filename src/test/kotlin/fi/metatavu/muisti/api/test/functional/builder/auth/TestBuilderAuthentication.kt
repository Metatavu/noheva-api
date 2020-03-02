package fi.metatavu.muisti.api.test.functional.auth

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.test.functional.builder.impl.FileTestBuilderResource
import fi.metatavu.muisti.api.test.functional.impl.*
import fi.metatavu.muisti.api.test.functional.settings.TestSettings
import java.io.IOException

/**
 * Test builder authentication
 *
 * @author Antti Leppä
 *
 * Constructor
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 */
class TestBuilderAuthentication(testBuilder: AbstractTestBuilder<ApiClient>, accessTokenProvider: AccessTokenProvider) : AuthorizedTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

  private var accessTokenProvider: AccessTokenProvider? = accessTokenProvider
  private var exhibitions: ExhibitionsTestBuilderResource? = null
  private var visitorSessions: VisitorSessionTestBuilderResource? = null
  private var exhibitionRooms: ExhibitionRoomTestBuilderResource? = null
  private var exhibitionDeviceGroups: ExhibitionDeviceGroupTestBuilderResource? = null
  private var exhibitionDeviceModels: ExhibitionDeviceModelTestBuilderResource? = null
  private var exhibitionDevices: ExhibitionDeviceTestBuilderResource? = null
  private var exhibitionPageLayouts: ExhibitionPageLayoutTestBuilderResource? = null
  private var exhibitionPages: ExhibitionPageTestBuilderResource? = null
  private var files: FileTestBuilderResource? = null

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
   * Returns test builder resource for exhibitionRooms
   *
   * @return test builder resource for exhibitionRooms
   * @throws IOException thrown when authentication fails
   */
  @kotlin.jvm.Throws(IOException::class)
  fun exhibitionRooms(): ExhibitionRoomTestBuilderResource {
    if (exhibitionRooms == null) {
      exhibitionRooms = ExhibitionRoomTestBuilderResource(getTestBuilder(), this.accessTokenProvider, createClient())
    }

    return exhibitionRooms!!
  }

  /**
   * Returns test builder resource for exhibitionDevices
   *
   * @return test builder resource for exhibitionDevices
   * @throws IOException thrown when authentication fails
   */
  @kotlin.jvm.Throws(IOException::class)
  fun exhibitionDevices(): ExhibitionDeviceTestBuilderResource {
    if (exhibitionDevices == null) {
      exhibitionDevices = ExhibitionDeviceTestBuilderResource(getTestBuilder(), this.accessTokenProvider, createClient())
    }

    return exhibitionDevices!!
  }

  /**
   * Returns test builder resource for exhibitionGroups
   *
   * @return test builder resource for exhibitionGroups
   * @throws IOException thrown when authentication fails
   */
  @kotlin.jvm.Throws(IOException::class)
  fun exhibitionDeviceGroups(): ExhibitionDeviceGroupTestBuilderResource {
    if (exhibitionDeviceGroups == null) {
      exhibitionDeviceGroups = ExhibitionDeviceGroupTestBuilderResource(getTestBuilder(), this.accessTokenProvider, createClient())
    }

    return exhibitionDeviceGroups!!
  }

  /**
   * Returns test builder resource for exhibitionDevices
   *
   * @return test builder resource for exhibitionDevices
   * @throws IOException thrown when authentication fails
   */
  @kotlin.jvm.Throws(IOException::class)
  fun exhibitionDeviceModels(): ExhibitionDeviceModelTestBuilderResource {
    if (exhibitionDeviceModels == null) {
      exhibitionDeviceModels = ExhibitionDeviceModelTestBuilderResource(getTestBuilder(), this.accessTokenProvider, createClient())
    }

    return exhibitionDeviceModels!!
  }

  /**
   * Returns test builder resource for exhibitionPageLayouts
   *
   * @return test builder resource for exhibitionPageLayouts
   * @throws IOException thrown when authentication fails
   */
  @kotlin.jvm.Throws(IOException::class)
  fun exhibitionPageLayouts(): ExhibitionPageLayoutTestBuilderResource {
    if (exhibitionPageLayouts == null) {
      exhibitionPageLayouts = ExhibitionPageLayoutTestBuilderResource(getTestBuilder(), this.accessTokenProvider, createClient())
    }

    return exhibitionPageLayouts!!
  }

  /**
   * Returns test builder resource for exhibitionPages
   *
   * @return test builder resource for exhibitionPages
   * @throws IOException thrown when authentication fails
   */
  @kotlin.jvm.Throws(IOException::class)
  fun exhibitionPages(): ExhibitionPageTestBuilderResource {
    if (exhibitionPages == null) {
      exhibitionPages = ExhibitionPageTestBuilderResource(getTestBuilder(), this.accessTokenProvider, createClient())
    }

    return exhibitionPages!!
  }

  /**
   * Returns test builder resource for files
   *
   * @return test builder resource for files
   * @throws IOException thrown when authentication fails
   */
  @kotlin.jvm.Throws(IOException::class)
  fun files(): FileTestBuilderResource {
    if (files == null) {
      files = FileTestBuilderResource(getTestBuilder())
    }

    return files!!
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