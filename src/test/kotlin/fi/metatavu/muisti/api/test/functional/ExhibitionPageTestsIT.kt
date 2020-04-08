package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition page API
 *
 * @author Antti Leppä
 */
class ExhibitionPageTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionPage() {
        TestBuilder().use {
            val createdPageSubscription = it.mqtt().subscribe<MqttExhibitionPageCreate>(MqttExhibitionPageCreate::class.java,"pages/create")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val deviceModel = it.admin().deviceModels().create()
            val defaultPageLayout = it.admin().pageLayouts().create(deviceModel)
            val layout = it.admin().pageLayouts().create(defaultPageLayout)
            val layoutId = layout.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!
            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val groupId: UUID = group.id!!
            val model = it.admin().deviceModels().create()
            val modelId = model.id!!
            val deviceId = it.admin().exhibitionDevices().create(exhibitionId, groupId, modelId).id!!
            val contentVersion = it.admin().exhibitionContentVersions().create(exhibitionId)
            val contentVersionId = contentVersion.id!!
            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibitionId, layoutId, deviceId, contentVersionId)
            assertNotNull(createdExhibitionPage)
            it.admin().exhibitions().assertCreateFail(400, "")

            assertJsonsEqual(listOf(MqttExhibitionPageCreate(exhibitionId = exhibitionId, id = createdExhibitionPage.id)), createdPageSubscription.getMessages(1))
        }
   }

    @Test
    fun testFindExhibitionPage() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val deviceModel = it.admin().deviceModels().create()
            val defaultPageLayout = it.admin().pageLayouts().create(deviceModel)
            val layout = it.admin().pageLayouts().create(defaultPageLayout)
            val layoutId = layout.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionPageId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val model = it.admin().deviceModels().create()
            val deviceId = it.admin().exhibitionDevices().create(exhibitionId, group.id!!, model.id!!).id!!
            val contentVersion = it.admin().exhibitionContentVersions().create(exhibitionId)
            val contentVersionId = contentVersion.id!!

            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibitionId = exhibitionId, layoutId = layoutId, deviceId = deviceId, contentVersionId = contentVersionId)
            val createdExhibitionPageId = createdExhibitionPage.id!!

            it.admin().exhibitionPages().assertFindFail(404, exhibitionId, nonExistingExhibitionPageId)
            it.admin().exhibitionPages().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionPageId)
            it.admin().exhibitionPages().assertFindFail(404, nonExistingExhibitionId, createdExhibitionPageId)
            assertNotNull(it.admin().exhibitionPages().findExhibitionPage(exhibitionId, createdExhibitionPageId))
        }
    }

    @Test
    fun testListExhibitionPages() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val deviceModel = it.admin().deviceModels().create()
            val defaultPageLayout = it.admin().pageLayouts().create(deviceModel)
            val layout = it.admin().pageLayouts().create(defaultPageLayout)
            val layoutId = layout.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val model = it.admin().deviceModels().create()
            val deviceId = it.admin().exhibitionDevices().create(exhibitionId, group.id!!, model.id!!).id!!
            val contentVersion = it.admin().exhibitionContentVersions().create(exhibitionId)
            val contentVersionId = contentVersion.id!!

            it.admin().exhibitionPages().assertListFail(404, nonExistingExhibitionId, deviceId, null)
            assertEquals(0, it.admin().exhibitionPages().listExhibitionPages(exhibitionId, deviceId, null).size)

            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibitionId, layoutId, deviceId, contentVersionId)
            val createdExhibitionPageId = createdExhibitionPage.id!!
            val exhibitionPage = it.admin().exhibitionPages().listExhibitionPages(exhibitionId, deviceId, null)
            assertEquals(1, exhibitionPage.size)
            assertEquals(createdExhibitionPageId, exhibitionPage[0].id)
            it.admin().exhibitionPages().delete(exhibitionId, createdExhibitionPageId)
            assertEquals(0, it.admin().exhibitionPages().listExhibitionPages(exhibitionId, deviceId, null).size)
        }
    }

    @Test
    fun testListExhibitionPagesByContentVersion() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val model = it.admin().deviceModels().create()
            val deviceId = it.admin().exhibitionDevices().create(exhibitionId, group.id!!, model.id!!).id!!
            val layout = it.admin().pageLayouts().create(model)
            val layoutId = layout.id!!

            val contentVersion1 = it.admin().exhibitionContentVersions().create(exhibitionId)
            val contentVersion1Id = contentVersion1.id!!
            val contentVersion2 = it.admin().exhibitionContentVersions().create(exhibitionId)
            val contentVersion2Id = contentVersion2.id!!

            it.admin().exhibitionPages().assertListFail(400, exhibitionId, deviceId, UUID.randomUUID())
            assertEquals(0, it.admin().exhibitionPages().listExhibitionPages(exhibitionId, deviceId, contentVersion1Id).size)
            assertEquals(0, it.admin().exhibitionPages().listExhibitionPages(exhibitionId, deviceId, contentVersion2Id).size)

            val page1 = it.admin().exhibitionPages().create(exhibitionId, layoutId, deviceId, contentVersion1Id)
            val page2 = it.admin().exhibitionPages().create(exhibitionId, layoutId, deviceId, contentVersion2Id)

            val pages1 = it.admin().exhibitionPages().listExhibitionPages(exhibitionId, deviceId, contentVersion1Id)
            val pages2 = it.admin().exhibitionPages().listExhibitionPages(exhibitionId, deviceId, contentVersion2Id)

            assertEquals(1, pages1.size)
            assertEquals(pages1[0].id, page1.id)

            assertEquals(1, pages2.size)
            assertEquals(pages2[0].id, page2.id)

            it.admin().exhibitionPages().delete(exhibitionId, page1.id!!)

            assertEquals(0, it.admin().exhibitionPages().listExhibitionPages(exhibitionId, deviceId, contentVersion1Id).size)
            assertEquals(1, it.admin().exhibitionPages().listExhibitionPages(exhibitionId, deviceId, contentVersion2Id).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        TestBuilder().use {
            val updatedPageSubscription = it.mqtt().subscribe<MqttExhibitionPageUpdate>(MqttExhibitionPageUpdate::class.java,"pages/update")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val deviceModel = it.admin().deviceModels().create()
            val defaultPageLayout = it.admin().pageLayouts().create(deviceModel)
            val createLayout = it.admin().pageLayouts().create(defaultPageLayout)
            val createLayoutId = createLayout.id!!

            val updateDeviceModel = it.admin().deviceModels().create()
            val updateDefaultPageLayout = it.admin().pageLayouts().create(updateDeviceModel)
            val updateLayout = it.admin().pageLayouts().create(updateDefaultPageLayout)
            val updateLayoutId = updateLayout.id!!

            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val model = it.admin().deviceModels().create()
            val deviceId = it.admin().exhibitionDevices().create(exhibitionId, group.id!!, model.id!!).id!!
            val contentVersion = it.admin().exhibitionContentVersions().create(exhibitionId)
            val contentVersionId = contentVersion.id!!

            val navigatePage = it.admin().exhibitionPages().create(exhibitionId = exhibitionId, layoutId = createLayoutId, deviceId = deviceId, contentVersionId = contentVersionId)
            val navigatePageId = navigatePage.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val createResource = ExhibitionPageResource(
                id = "createresid",
                data = "https://example.com/image.png",
                type = ExhibitionPageResourceType.image
            )

            val createEvent = ExhibitionPageEvent(
                action = ExhibitionPageEventActionType.navigate,
                properties = arrayOf(
                    ExhibitionPageEventProperty(
                        name = "pageId",
                        type = ExhibitionPageEventPropertyType.string,
                        value = navigatePageId.toString()
                    )
                )
            )

            val createEventTrigger = ExhibitionPageEventTrigger(
                events = arrayOf(createEvent),
                clickViewId =  "createviewid",
                delay = 0,
                next = arrayOf()
            )

            val createPage = ExhibitionPage(
                layoutId = createLayoutId,
                deviceId = deviceId,
                name = "create page",
                resources = arrayOf(createResource),
                eventTriggers = arrayOf(createEventTrigger),
                contentVersionId = contentVersionId
            )

            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibitionId, createPage)
            val createdExhibitionPageId = createdExhibitionPage.id!!
            val foundCreatedExhibitionPage = it.admin().exhibitionPages().findExhibitionPage(exhibitionId, createdExhibitionPageId)

            assertEquals(createPage.name, createdExhibitionPage.name)
            assertEquals(createPage.layoutId, createdExhibitionPage.layoutId)
            assertJsonsEqual(createdExhibitionPage, foundCreatedExhibitionPage)
            assertJsonsEqual(createPage.eventTriggers, createdExhibitionPage.eventTriggers)
            assertJsonsEqual(createPage.resources, createdExhibitionPage.resources)

            val updateResource = ExhibitionPageResource(
                id = "updateresid",
                data = "https://example.com/updated.png",
                type = ExhibitionPageResourceType.video
            )

            val updateEvent = ExhibitionPageEvent(
                action = ExhibitionPageEventActionType.hide,
                properties = arrayOf(
                    ExhibitionPageEventProperty(
                        name = "background",
                        type = ExhibitionPageEventPropertyType.color,
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
                next = arrayOf()
            )

            val updatePage = ExhibitionPage(
                id = createdExhibitionPageId,
                layoutId = updateLayoutId,
                deviceId = deviceId,
                name = "update page",
                resources = arrayOf(updateResource),
                eventTriggers = arrayOf(updateEventTrigger),
                contentVersionId = contentVersionId
            )

            val updatedExhibitionPage = it.admin().exhibitionPages().updateExhibitionPage(exhibitionId, updatePage)
            val foundUpdateExhibitionPage = it.admin().exhibitionPages().findExhibitionPage(exhibitionId, createdExhibitionPageId)
            assertJsonsEqual(updatedExhibitionPage?.copy( modifiedAt = foundUpdateExhibitionPage?.modifiedAt ), foundUpdateExhibitionPage)

            assertNotNull(updatedExhibitionPage)
            assertEquals(updatePage.name, updatedExhibitionPage!!.name)
            assertEquals(updatePage.layoutId, updatedExhibitionPage.layoutId)
            assertJsonsEqual(updatedExhibitionPage, updatedExhibitionPage)
            assertJsonsEqual(updatePage.eventTriggers, updatedExhibitionPage.eventTriggers)
            assertJsonsEqual(updatePage.resources, updatedExhibitionPage.resources)

            it.admin().exhibitionPages().assertUpdateFail(404, nonExistingExhibitionId, updatePage)
            it.admin().exhibitionPages().assertUpdateFail(400, exhibitionId, updatePage.copy( layoutId = UUID.randomUUID()))

            assertJsonsEqual(listOf(MqttExhibitionPageUpdate(exhibitionId = exhibitionId, id = createdExhibitionPage.id)), updatedPageSubscription.getMessages(1))
        }
    }

    @Test
    fun testDeleteExhibition() {
        TestBuilder().use {
            val deletePageSubscription = it.mqtt().subscribe<MqttExhibitionPageDelete>(MqttExhibitionPageDelete::class.java,"pages/delete")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val deviceModel = it.admin().deviceModels().create()
            val defaultPageLayout = it.admin().pageLayouts().create(deviceModel)
            val layout = it.admin().pageLayouts().create(defaultPageLayout)
            val layoutId = layout.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val model = it.admin().deviceModels().create()
            val deviceId = it.admin().exhibitionDevices().create(exhibitionId, group.id!!, model.id!!).id!!
            val contentVersion = it.admin().exhibitionContentVersions().create(exhibitionId)
            val contentVersionId = contentVersion.id!!

            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibitionId = exhibitionId, layoutId = layoutId, deviceId = deviceId, contentVersionId = contentVersionId)
            val createdExhibitionPageId = createdExhibitionPage.id!!

            assertNotNull(it.admin().exhibitionPages().findExhibitionPage(exhibitionId, createdExhibitionPageId))
            it.admin().exhibitionPages().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionPages().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionPageId)
            it.admin().exhibitionPages().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().exhibitionPages().delete(exhibitionId, createdExhibitionPage)

            it.admin().exhibitionPages().assertDeleteFail(404, exhibitionId, createdExhibitionPageId)

            assertJsonsEqual(listOf(MqttExhibitionPageDelete(exhibitionId = exhibitionId, id = createdExhibitionPage.id)), deletePageSubscription.getMessages(1))
        }
    }

}