package fi.metatavu.noheva.api.test.functional

import fi.metatavu.noheva.api.client.models.GroupContentVersion
import fi.metatavu.noheva.api.client.models.GroupContentVersionStatus
import fi.metatavu.noheva.api.test.functional.resources.KeycloakResource
import fi.metatavu.noheva.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test class for testing group content versions API
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
  QuarkusTestResource(MysqlResource::class),
  QuarkusTestResource(KeycloakResource::class)
)
class GroupContentVersionTestsIT: AbstractFunctionalTest() {

  @Test
  fun testCreateGroupContentVersion() {
      createTestBuilder().use {
        val exhibitionId = it.admin.exhibitions.create().id!!
        val createContentVersionId = it.admin.contentVersions.create(exhibitionId).id!!
        val createdFloorId = it.admin.exhibitionFloors.create(exhibitionId).id!!
        val createdRoomId = it.admin.exhibitionRooms.create(exhibitionId, createdFloorId).id!!
        val createdDeviceGroupId = it.admin.exhibitionDeviceGroups.create(
          exhibitionId = exhibitionId,
          roomId = createdRoomId,
          name = "Group 1"
        ).id!!

        val groupContentVersionToCreate = GroupContentVersion(
            name = "Group name",
            status = GroupContentVersionStatus.NOTSTARTED,
            deviceGroupId = createdDeviceGroupId,
            contentVersionId = createContentVersionId
        )

        val createdGroupContentVersion = it.admin.groupContentVersions.create(exhibitionId, groupContentVersionToCreate)
        assertNotNull(createdGroupContentVersion)
      }
  }

  @Test
  fun testUpdateGroupContentVersion() {
    createTestBuilder().use {
      val exhibitionId = it.admin.exhibitions.create().id!!
      val createdContentVersionId = it.admin.contentVersions.create(exhibitionId).id!!
      val createdFloorId = it.admin.exhibitionFloors.create(exhibitionId).id!!
      val createdRoomId = it.admin.exhibitionRooms.create(exhibitionId, createdFloorId).id!!
      val createdDeviceGroupId = it.admin.exhibitionDeviceGroups.create(
        exhibitionId = exhibitionId,
        roomId = createdRoomId,
        name = "Group 1"
      ).id!!

      val groupContentVersionToCreate = GroupContentVersion(
        name = "Group name",
        status = GroupContentVersionStatus.NOTSTARTED,
        deviceGroupId = createdDeviceGroupId,
        contentVersionId = createdContentVersionId
      )

      val createdGroupContentVersion = it.admin.groupContentVersions.create(exhibitionId, groupContentVersionToCreate)
      assertNotNull(createdGroupContentVersion)

      val groupContentVersionToUpdate = GroupContentVersion(
        id = createdGroupContentVersion.id!!,
        name = "Updated name",
        status = GroupContentVersionStatus.INPROGRESS,
        deviceGroupId = createdDeviceGroupId,
        contentVersionId = createdContentVersionId
      )

      val updatedGroupContentVersion = it.admin.groupContentVersions.updateGroupContentVersion(exhibitionId, groupContentVersionToUpdate)
      assertNotNull(updatedGroupContentVersion)
      assertEquals(createdGroupContentVersion.id, updatedGroupContentVersion.id!!)
      assertEquals(createdGroupContentVersion.contentVersionId, updatedGroupContentVersion.contentVersionId)
      assertEquals(createdGroupContentVersion.deviceGroupId, updatedGroupContentVersion.deviceGroupId)
      assertNotEquals(createdGroupContentVersion.status, updatedGroupContentVersion.status)
      assertEquals(GroupContentVersionStatus.INPROGRESS, updatedGroupContentVersion.status)
    }
  }

  @Test
  fun testFindGroupContentVersion() {
    createTestBuilder().use {
      val exhibitionId = it.admin.exhibitions.create().id!!
      val nonExistingExhibitionId = UUID.randomUUID()
      val nonExistingGroupContentVersionId = UUID.randomUUID()

      val createdContentVersionId = it.admin.contentVersions.create(exhibitionId).id!!
      val createdFloorId = it.admin.exhibitionFloors.create(exhibitionId).id!!
      val createdRoomId = it.admin.exhibitionRooms.create(exhibitionId, createdFloorId).id!!
      val createdDeviceGroupId = it.admin.exhibitionDeviceGroups.create(
        exhibitionId = exhibitionId,
        roomId = createdRoomId,
        name = "Group 1"
      ).id!!

      val groupContentVersionToCreate = GroupContentVersion(
        name = "Group name",
        status = GroupContentVersionStatus.NOTSTARTED,
        deviceGroupId = createdDeviceGroupId,
        contentVersionId = createdContentVersionId
      )

      val createdGroupContentVersion = it.admin.groupContentVersions.create(exhibitionId, groupContentVersionToCreate)

      it.admin.groupContentVersions.assertFindFail(404, exhibitionId, nonExistingGroupContentVersionId)
      it.admin.groupContentVersions.assertFindFail(404, nonExistingExhibitionId, nonExistingGroupContentVersionId)
      it.admin.groupContentVersions.assertFindFail(404, nonExistingExhibitionId, createdContentVersionId)
      assertNotNull(it.admin.groupContentVersions.findGroupContentVersion(exhibitionId, createdGroupContentVersion.id!!))
    }
  }

  @Test
  fun testListGroupContentVersions() {
      createTestBuilder().use {
        val exhibitionId = it.admin.exhibitions.create().id!!
        val nonExistingExhibitionId = UUID.randomUUID()

        it.admin.groupContentVersions.assertListFail(expectedStatus = 404, exhibitionId = nonExistingExhibitionId, contentVersionId = null, deviceGroupId = null)
        assertEquals(0, it.admin.groupContentVersions.listGroupContentVersions(exhibitionId = exhibitionId, contentVersionId = null, deviceGroupId = null).size)

        val contentVersionId1 = it.admin.contentVersions.create(exhibitionId).id!!
        val contentVersionId2 = it.admin.contentVersions.create(exhibitionId).id!!
        val contentVersionId3 = it.admin.contentVersions.create(exhibitionId).id!!
        val createdFloorId = it.admin.exhibitionFloors.create(exhibitionId).id!!
        val createdRoomId = it.admin.exhibitionRooms.create(exhibitionId, createdFloorId).id!!
        val createdDeviceGroupId = it.admin.exhibitionDeviceGroups.create(
          exhibitionId = exhibitionId,
          roomId = createdRoomId,
          name = "Group 1"
        ).id!!

        val groupContentVersion1 = it.admin.groupContentVersions.create(exhibitionId, GroupContentVersion(
          name = "Group content 1",
          status = GroupContentVersionStatus.NOTSTARTED,
          deviceGroupId = createdDeviceGroupId,
          contentVersionId = contentVersionId1
        ))

        val groupContentVersion2 = it.admin.groupContentVersions.create(exhibitionId, GroupContentVersion(
          name = "Group content 2",
          status = GroupContentVersionStatus.NOTSTARTED,
          deviceGroupId = createdDeviceGroupId,
          contentVersionId = contentVersionId2
        ))

        it.admin.groupContentVersions.assertCount(2, exhibitionId = exhibitionId, contentVersionId = null, deviceGroupId = null)
        it.admin.groupContentVersions.assertCount(0, exhibitionId = exhibitionId, contentVersionId = contentVersionId3, deviceGroupId = null)
        it.admin.groupContentVersions.assertCount(2, exhibitionId = exhibitionId, contentVersionId = null, deviceGroupId = createdDeviceGroupId)
        it.admin.groupContentVersions.assertCount(1, exhibitionId = exhibitionId, contentVersionId = contentVersionId1, deviceGroupId = null)
        it.admin.groupContentVersions.assertCount(1, exhibitionId = exhibitionId, contentVersionId = contentVersionId1, deviceGroupId = createdDeviceGroupId)

        val groupContentVersions1 = it.admin.groupContentVersions.listGroupContentVersions(exhibitionId = exhibitionId, contentVersionId = contentVersionId1, deviceGroupId = createdDeviceGroupId)
        assertEquals(1, groupContentVersions1.size)
        assertEquals(groupContentVersion1.id!!, groupContentVersions1[0].id)
        assertEquals(contentVersionId1, groupContentVersions1[0].contentVersionId)

        val groupContentVersions2 = it.admin.groupContentVersions.listGroupContentVersions(exhibitionId = exhibitionId, contentVersionId = contentVersionId2, deviceGroupId = null)
        assertEquals(1, groupContentVersions2.size)
        assertEquals(groupContentVersion2.id!!, groupContentVersions2[0].id)
        assertEquals(contentVersionId2, groupContentVersions2[0].contentVersionId)

        it.admin.groupContentVersions.delete(exhibitionId, groupContentVersion1.id)
        it.admin.groupContentVersions.assertCount(1, exhibitionId = exhibitionId, contentVersionId = null, deviceGroupId = null)
      }
  }

  @Test
  fun testDeleteGroupContentVersion() {
    createTestBuilder().use {

      val exhibition = it.admin.exhibitions.create()
      val exhibitionId = exhibition.id!!
      val contentVersionId = it.admin.contentVersions.create(exhibitionId).id!!
      val deviceGroupId = createDefaultDeviceGroup(testBuilder = it, exhibition = exhibition).id!!

      val groupContentVersionToCreate = GroupContentVersion(
        name = "Group content",
        status = GroupContentVersionStatus.NOTSTARTED,
        deviceGroupId = deviceGroupId,
        contentVersionId = contentVersionId
      )

      val groupContentVersionId = it.admin.groupContentVersions.create(exhibitionId = exhibitionId, payload = groupContentVersionToCreate).id!!

      it.admin.groupContentVersions.assertDeleteFail(expectedStatus = 404, exhibitionId =  UUID.randomUUID(), groupContentVersionId = groupContentVersionId)
      it.admin.groupContentVersions.assertDeleteFail(expectedStatus = 404, exhibitionId = exhibitionId, groupContentVersionId = UUID.randomUUID())
      it.admin.groupContentVersions.assertDeleteFail(expectedStatus = 404, exhibitionId = UUID.randomUUID(), groupContentVersionId = UUID.randomUUID())

      it.admin.groupContentVersions.assertCount(1, exhibitionId = exhibitionId, contentVersionId = null, deviceGroupId = null)
      assertNotNull(it.admin.groupContentVersions.findGroupContentVersion(exhibitionId = exhibitionId, groupContentVersionId = groupContentVersionId))
      it.admin.groupContentVersions.delete(exhibitionId = exhibitionId, groupContentVersionId = groupContentVersionId)
      it.admin.groupContentVersions.assertCount(0, exhibitionId = exhibitionId, contentVersionId = null, deviceGroupId = null)
      it.admin.groupContentVersions.assertFindFail(expectedStatus = 404, exhibitionId = exhibitionId, groupContentVersionId = groupContentVersionId)
      it.admin.groupContentVersions.assertDeleteFail(expectedStatus = 404, exhibitionId = exhibitionId, groupContentVersionId = groupContentVersionId)
    }
  }

}