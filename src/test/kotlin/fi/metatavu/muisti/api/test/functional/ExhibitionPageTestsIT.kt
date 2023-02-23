package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MqttResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test class for testing exhibition page API
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
@TestProfile(DefaultTestProfile::class)
class ExhibitionPageTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionPage() {
        createTestBuilder().use {
            val createdPageSubscription = it.mqtt.subscribe(MqttExhibitionPageCreate::class.java,"pages/create")
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val deviceModel = it.admin.deviceModels.create()
            val defaultPageLayout = it.admin.pageLayouts.create(deviceModel)
            val layout = it.admin.pageLayouts.create(defaultPageLayout)
            val layoutId = layout.id!!
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!
            val deviceGroup = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val deviceGroupId: UUID = deviceGroup.id!!
            val model = it.admin.deviceModels.create()
            val modelId = model.id!!
            val deviceId = it.admin.exhibitionDevices.create(exhibitionId, deviceGroupId, modelId).id!!
            val contentVersion = it.admin.contentVersions.create(exhibitionId)
            val contentVersionId = contentVersion.id!!

            it.admin.groupContentVersions.create(
                exhibitionId = exhibitionId,
                payload = GroupContentVersion(
                    name = "default",
                    contentVersionId = contentVersionId,
                    deviceGroupId = deviceGroupId,
                    status = GroupContentVersionStatus.INPROGRESS
                )
            )

            val createdExhibitionPage = it.admin.exhibitionPages.create(exhibitionId, layoutId, deviceId, contentVersionId)
            assertNotNull(createdExhibitionPage)
            assertJsonsEqual(listOf(MqttExhibitionPageCreate(exhibitionId = exhibitionId, id = createdExhibitionPage.id)), createdPageSubscription.getMessages(1))
        }
   }

    @Test
    fun testFindExhibitionPage() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val deviceModel = it.admin.deviceModels.create()
            val defaultPageLayout = it.admin.pageLayouts.create(deviceModel)
            val layout = it.admin.pageLayouts.create(defaultPageLayout)
            val layoutId = layout.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionPageId = UUID.randomUUID()
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val deviceGroup = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val deviceGroupId = deviceGroup.id!!
            val model = it.admin.deviceModels.create()
            val deviceId = it.admin.exhibitionDevices.create(exhibitionId, deviceGroupId, model.id!!).id!!
            val contentVersion = it.admin.contentVersions.create(exhibitionId)
            val contentVersionId = contentVersion.id!!

            it.admin.groupContentVersions.create(
                exhibitionId = exhibitionId,
                payload = GroupContentVersion(
                    name = "default",
                    contentVersionId = contentVersionId,
                    deviceGroupId = deviceGroupId,
                    status = GroupContentVersionStatus.INPROGRESS
                )
            )

            val createdExhibitionPage = it.admin.exhibitionPages.create(exhibitionId = exhibitionId, layoutId = layoutId, deviceId = deviceId, contentVersionId = contentVersionId)
            val createdExhibitionPageId = createdExhibitionPage.id!!

            it.admin.exhibitionPages.assertFindFail(404, exhibitionId, nonExistingExhibitionPageId)
            it.admin.exhibitionPages.assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionPageId)
            it.admin.exhibitionPages.assertFindFail(404, nonExistingExhibitionId, createdExhibitionPageId)
            assertNotNull(it.admin.exhibitionPages.findExhibitionPage(exhibitionId, createdExhibitionPageId))
        }
    }

    @Test
    fun testListExhibitionPages() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val deviceModel = it.admin.deviceModels.create()
            val defaultPageLayout = it.admin.pageLayouts.create(deviceModel)
            val layout = it.admin.pageLayouts.create(defaultPageLayout)
            val layoutId = layout.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val deviceGroup = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val deviceGroupId = deviceGroup.id!!
            val model = it.admin.deviceModels.create()
            val deviceId = it.admin.exhibitionDevices.create(exhibitionId, deviceGroupId, model.id!!).id!!
            val contentVersion = it.admin.contentVersions.create(exhibitionId)
            val contentVersionId = contentVersion.id!!

            it.admin.groupContentVersions.create(
                exhibitionId = exhibitionId,
                payload = GroupContentVersion(
                    name = "default",
                    contentVersionId = contentVersionId,
                    deviceGroupId = deviceGroupId,
                    status = GroupContentVersionStatus.INPROGRESS
                )
            )

            it.admin.exhibitionPages.assertListFail(404, nonExistingExhibitionId, deviceId, null, null)
            assertEquals(0, it.admin.exhibitionPages.listExhibitionPages(exhibitionId, deviceId, null, null).size)

            val createdExhibitionPage = it.admin.exhibitionPages.create(exhibitionId, layoutId, deviceId, contentVersionId)
            val createdExhibitionPageId = createdExhibitionPage.id!!
            val exhibitionPage = it.admin.exhibitionPages.listExhibitionPages(exhibitionId, deviceId, null, null)
            assertEquals(1, exhibitionPage.size)
            assertEquals(createdExhibitionPageId, exhibitionPage[0].id)
            it.admin.exhibitionPages.delete(exhibitionId, createdExhibitionPageId)
            assertEquals(0, it.admin.exhibitionPages.listExhibitionPages(exhibitionId, deviceId, null, null).size)
        }
    }

    @Test
    fun testListExhibitionPagesByContentVersion() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val deviceGroup = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val deviceGroupId = deviceGroup.id!!
            val model = it.admin.deviceModels.create()
            val deviceId = it.admin.exhibitionDevices.create(exhibitionId, deviceGroupId, model.id!!).id!!
            val layout = it.admin.pageLayouts.create(model)
            val layoutId = layout.id!!

            val contentVersion1 = it.admin.contentVersions.create(exhibitionId)
            val contentVersion1Id = contentVersion1.id!!
            val contentVersion2 = it.admin.contentVersions.create(exhibitionId)
            val contentVersion2Id = contentVersion2.id!!

            it.admin.exhibitionPages.assertListFail(400, exhibitionId, deviceId, UUID.randomUUID(), null)
            assertEquals(0, it.admin.exhibitionPages.listExhibitionPages(exhibitionId, deviceId, contentVersion1Id, null).size)
            assertEquals(0, it.admin.exhibitionPages.listExhibitionPages(exhibitionId, deviceId, contentVersion2Id, null).size)

            listOf(contentVersion1Id, contentVersion2Id).forEach { contentVersionId ->
                it.admin.groupContentVersions.create(
                    exhibitionId = exhibitionId,
                    payload = GroupContentVersion(
                        name = "default",
                        contentVersionId = contentVersionId,
                        deviceGroupId = deviceGroupId,
                        status = GroupContentVersionStatus.INPROGRESS
                    )
                )
            }

            val page1 = it.admin.exhibitionPages.create(exhibitionId, layoutId, deviceId, contentVersion1Id)
            val page2 = it.admin.exhibitionPages.create(exhibitionId, layoutId, deviceId, contentVersion2Id)

            val pages1 = it.admin.exhibitionPages.listExhibitionPages(exhibitionId, deviceId, contentVersion1Id, null)
            val pages2 = it.admin.exhibitionPages.listExhibitionPages(exhibitionId, deviceId, contentVersion2Id, null)

            val pagesFilteredByLayout = it.admin.exhibitionPages.listExhibitionPages(exhibitionId, null, null, layoutId)

            assertEquals(1, pages1.size)
            assertEquals(pages1[0].id, page1.id)

            assertEquals(1, pages2.size)
            assertEquals(pages2[0].id, page2.id)

            assertEquals(2, pagesFilteredByLayout.size)

            it.admin.exhibitionPages.delete(exhibitionId, page1.id!!)

            assertEquals(0, it.admin.exhibitionPages.listExhibitionPages(exhibitionId, deviceId, contentVersion1Id, null).size)
            assertEquals(1, it.admin.exhibitionPages.listExhibitionPages(exhibitionId, deviceId, contentVersion2Id, null).size)
        }
    }

    @Test
    fun testUpdatePage() {
        createTestBuilder().use {
            val updatedPageSubscription = it.mqtt.subscribe(MqttExhibitionPageUpdate::class.java,"pages/update")

            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val deviceModel = it.admin.deviceModels.create()
            val defaultPageLayout = it.admin.pageLayouts.create(deviceModel)
            val createLayout = it.admin.pageLayouts.create(defaultPageLayout)
            val createLayoutId = createLayout.id!!

            val updateDeviceModel = it.admin.deviceModels.create()
            val updateDefaultPageLayout = it.admin.pageLayouts.create(updateDeviceModel)
            val updateLayout = it.admin.pageLayouts.create(updateDefaultPageLayout)
            val updateLayoutId = updateLayout.id!!

            val deviceGroup = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val deviceGroupId = deviceGroup.id!!
            val model = it.admin.deviceModels.create()
            val deviceId = it.admin.exhibitionDevices.create(exhibitionId, deviceGroupId, model.id!!).id!!
            val contentVersion = it.admin.contentVersions.create(exhibitionId)
            val contentVersionId = contentVersion.id!!

            it.admin.groupContentVersions.create(
                exhibitionId = exhibitionId,
                payload = GroupContentVersion(
                    name = "default",
                    contentVersionId = contentVersionId,
                    deviceGroupId = deviceGroupId,
                    status = GroupContentVersionStatus.INPROGRESS
                )
            )

            val navigatePage = it.admin.exhibitionPages.create(exhibitionId = exhibitionId, layoutId = createLayoutId, deviceId = deviceId, contentVersionId = contentVersionId)
            val navigatePageId = navigatePage.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val createResource = ExhibitionPageResource(
                id = "createresid",
                data = "https://example.com/IMAGE.png",
                type = ExhibitionPageResourceType.IMAGE
            )

            val createEvent = ExhibitionPageEvent(
                action = ExhibitionPageEventActionType.NAVIGATE,
                properties = arrayOf(
                    ExhibitionPageEventProperty(
                        name = "pageId",
                        type = ExhibitionPageEventPropertyType.STRING,
                        value = navigatePageId.toString()
                    )
                )
            )

            val createEventTrigger = ExhibitionPageEventTrigger(
                events = arrayOf(createEvent),
                clickViewId =  "createviewid",
                delay = 0,
                next = arrayOf(),
                id = UUID.randomUUID(),
                name = "Create View"
            )

            val fadeTransition = Transition(
                    duration = 300,
                    animation = Animation.FADE,
                    timeInterpolation = AnimationTimeInterpolation.ACCELERATE
            )

            val createEnterTransitions : Array<ExhibitionPageTransition> = arrayOf(
                    ExhibitionPageTransition(
                            transition = fadeTransition
                    )
            )

            val morphTransition = Transition (
                    duration = 300,
                    animation = Animation.MORPH,
                    timeInterpolation = AnimationTimeInterpolation.BOUNCE
            )

            val morphOptions = ExhibitionPageTransitionOptions(
                ExhibitionPageTransitionOptionsMorph(
                    views = arrayOf(ExhibitionPageTransitionOptionsMorphView(
                        sourceId = defaultPageLayout.id.toString(),
                        targetId = navigatePageId.toString()
                    ))
                )
            )

            val createExitTransitions: Array<ExhibitionPageTransition> = arrayOf(
                    ExhibitionPageTransition(
                            transition = morphTransition,
                            options = morphOptions
                    )
            )

            val createPage = ExhibitionPage(
                layoutId = createLayoutId,
                deviceId = deviceId,
                name = "create page",
                orderNumber = 0,
                resources = arrayOf(createResource),
                eventTriggers = arrayOf(createEventTrigger),
                contentVersionId = contentVersionId,
                enterTransitions = createEnterTransitions,
                exitTransitions = createExitTransitions
            )

            val createdExhibitionPage = it.admin.exhibitionPages.create(exhibitionId, createPage)
            val createdExhibitionPageId = createdExhibitionPage.id!!
            val foundCreatedExhibitionPage = it.admin.exhibitionPages.findExhibitionPage(exhibitionId, createdExhibitionPageId)

            assertEquals(createPage.name, createdExhibitionPage.name)
            assertEquals(createPage.layoutId, createdExhibitionPage.layoutId)
            assertJsonsEqual(createdExhibitionPage, foundCreatedExhibitionPage)
            assertJsonsEqual(createPage.eventTriggers, createdExhibitionPage.eventTriggers)
            assertJsonsEqual(createPage.resources, createdExhibitionPage.resources)
            assertJsonsEqual(createPage.enterTransitions, createdExhibitionPage.enterTransitions)
            assertJsonsEqual(createPage.exitTransitions, createdExhibitionPage.exitTransitions)

            val updateResource = ExhibitionPageResource(
                id = "updateresid",
                data = "https://example.com/updated.png",
                type = ExhibitionPageResourceType.VIDEO,
                mode = PageResourceMode.SCRIPTED
            )

            val updateEvent = ExhibitionPageEvent(
                action = ExhibitionPageEventActionType.HIDE,
                properties = arrayOf(
                    ExhibitionPageEventProperty(
                        name = "background",
                        type = ExhibitionPageEventPropertyType.COLOR,
                        value = "#fff"
                    )
                )
            )

            val updateEventTrigger = ExhibitionPageEventTrigger(
                events = arrayOf(updateEvent),
                clickViewId =  "updateviewid",
                delay = 2,
                keyDown = "A",
                keyUp = "B",
                deviceGroupEvent = "groupevent",
                next = arrayOf(),
                id = UUID.randomUUID(),
                name = "Update View"
            )

            val updatedFadeTransition = Transition(
                    duration = 600,
                    animation = Animation.FADE,
                    timeInterpolation = AnimationTimeInterpolation.ACCELERATEDECELERATE
            )

            val updateEnterTransitions: Array<ExhibitionPageTransition> = arrayOf(
                    ExhibitionPageTransition(
                            transition = updatedFadeTransition
                    )
            )

            val updatedMorphTransition = Transition (
                    duration = 600,
                    animation = Animation.MORPH,
                    timeInterpolation = AnimationTimeInterpolation.ACCELERATEDECELERATE
            )

            val updateExitTransitions: Array<ExhibitionPageTransition> = arrayOf(
                    ExhibitionPageTransition(
                            transition = updatedMorphTransition,
                            options = morphOptions
                    )
            )

            val updatePage = ExhibitionPage(
                id = createdExhibitionPageId,
                layoutId = updateLayoutId,
                deviceId = deviceId,
                name = "update page",
                resources = arrayOf(updateResource),
                eventTriggers = arrayOf(updateEventTrigger),
                contentVersionId = contentVersionId,
                enterTransitions = updateEnterTransitions,
                exitTransitions = updateExitTransitions,
                orderNumber = 1
            )

            val updatedExhibitionPage = it.admin.exhibitionPages.updateExhibitionPage(exhibitionId, updatePage)
            val foundUpdateExhibitionPage = it.admin.exhibitionPages.findExhibitionPage(exhibitionId, createdExhibitionPageId)
            assertJsonsEqual(updatedExhibitionPage.copy( modifiedAt = foundUpdateExhibitionPage.modifiedAt ), foundUpdateExhibitionPage)

            assertNotNull(updatedExhibitionPage)
            assertEquals(updatePage.name, updatedExhibitionPage.name)
            assertEquals(updatePage.layoutId, updatedExhibitionPage.layoutId)
            assertJsonsEqual(updatedExhibitionPage, updatedExhibitionPage)
            assertJsonsEqual(updatePage.eventTriggers, updatedExhibitionPage.eventTriggers)
            assertJsonsEqual(updatePage.resources, updatedExhibitionPage.resources)
            assertJsonsEqual(updatePage.enterTransitions, updatedExhibitionPage.enterTransitions)
            assertJsonsEqual(updatePage.exitTransitions, updatedExhibitionPage.exitTransitions)

            it.admin.exhibitionPages.assertUpdateFail(404, nonExistingExhibitionId, updatePage)
            it.admin.exhibitionPages.assertUpdateFail(400, exhibitionId, updatePage.copy( layoutId = UUID.randomUUID()))

            assertJsonsEqual(listOf(MqttExhibitionPageUpdate(exhibitionId = exhibitionId, id = createdExhibitionPage.id)), updatedPageSubscription.getMessages(1))
        }
    }

    @Test
    fun testDeletePage() {
        createTestBuilder().use {
            val deletePageSubscription = it.mqtt.subscribe(MqttExhibitionPageDelete::class.java,"pages/delete")

            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val deviceModel = it.admin.deviceModels.create()
            val defaultPageLayout = it.admin.pageLayouts.create(deviceModel)
            val layout = it.admin.pageLayouts.create(defaultPageLayout)
            val layoutId = layout.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val deviceGroup = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val deviceGroupId = deviceGroup.id!!
            val model = it.admin.deviceModels.create()
            val deviceId = it.admin.exhibitionDevices.create(exhibitionId, deviceGroupId, model.id!!).id!!
            val contentVersion = it.admin.contentVersions.create(exhibitionId)
            val contentVersionId = contentVersion.id!!

            it.admin.groupContentVersions.create(
                exhibitionId = exhibitionId,
                payload = GroupContentVersion(
                    name = "default",
                    contentVersionId = contentVersionId,
                    deviceGroupId = deviceGroupId,
                    status = GroupContentVersionStatus.INPROGRESS
                )
            )

            val createdExhibitionPage = it.admin.exhibitionPages.create(exhibitionId = exhibitionId, layoutId = layoutId, deviceId = deviceId, contentVersionId = contentVersionId)
            val createdExhibitionPageId = createdExhibitionPage.id!!

            assertNotNull(it.admin.exhibitionPages.findExhibitionPage(exhibitionId, createdExhibitionPageId))
            it.admin.exhibitionPages.assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin.exhibitionPages.assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionPageId)
            it.admin.exhibitionPages.assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin.exhibitionPages.delete(exhibitionId, createdExhibitionPage)

            it.admin.exhibitionPages.assertDeleteFail(404, exhibitionId, createdExhibitionPageId)

            assertJsonsEqual(listOf(MqttExhibitionPageDelete(exhibitionId = exhibitionId, id = createdExhibitionPage.id)), deletePageSubscription.getMessages(1))
        }
    }

}