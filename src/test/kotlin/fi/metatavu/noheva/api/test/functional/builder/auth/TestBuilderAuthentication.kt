package fi.metatavu.noheva.api.test.functional.builder.auth

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenTestBuilderAuthentication
import fi.metatavu.noheva.api.client.infrastructure.ApiClient
import fi.metatavu.noheva.api.test.functional.builder.TestBuilder
import fi.metatavu.noheva.api.test.functional.builder.impl.*
import fi.metatavu.noheva.api.test.functional.settings.ApiTestSettings

/**
 * Test builder authentication
 *
 * @author Jari Nykänen
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    accessTokenProvider: AccessTokenProvider
): AccessTokenTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

    private var accessTokenProvider: AccessTokenProvider? = accessTokenProvider

    val exhibitions: ExhibitionsTestBuilderResource = ExhibitionsTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val visitorSessions: VisitorSessionTestBuilderResource = VisitorSessionTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val visitorSessionsV2: VisitorSessionV2TestBuilderResource = VisitorSessionV2TestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val visitors: VisitorTestBuilderResource = VisitorTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val exhibitionRooms: ExhibitionRoomTestBuilderResource = ExhibitionRoomTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val exhibitionFloors: ExhibitionFloorTestBuilderResource = ExhibitionFloorTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val exhibitionDeviceGroups: ExhibitionDeviceGroupTestBuilderResource = ExhibitionDeviceGroupTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val deviceModels: DeviceModelTestBuilderResource = DeviceModelTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val exhibitionDevices: ExhibitionDeviceTestBuilderResource = ExhibitionDeviceTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val rfidAntennas: RfidAntennaTestBuilderResource = RfidAntennaTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val pageLayouts: PageLayoutTestBuilderResource = PageLayoutTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val subLayouts: SubLayoutTestBuilderResource = SubLayoutTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val exhibitionPages: ExhibitionPageTestBuilderResource = ExhibitionPageTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val storedFiles: StoredFilesTestBuilderResource = StoredFilesTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val contentVersions: ContentVersionTestBuilderResource = ContentVersionTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val visitorVariables: VisitorVariableTestBuilderResource = VisitorVariableTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val devices: DevicesTestBuilderResource = DevicesTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())

    override fun createClient(authProvider: AccessTokenProvider): ApiClient {
        val result = ApiClient(ApiTestSettings.apiBasePath)
        ApiClient.accessToken = authProvider.accessToken
        return result
    }

}