package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ContentVersion
import fi.metatavu.muisti.api.client.models.GroupContentVersion
import fi.metatavu.muisti.api.client.models.GroupContentVersionStatus
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Test class for testing group content versions API
 *
 * @author Jari Nykänen
 */
class GroupContentVersionTestsIT: AbstractFunctionalTest() {

  @Test
  fun testCreateGroupContentVersion() {
      ApiTestBuilder().use {
        val exhibitionId = it.admin().exhibitions().create().id!!
        val createContentVersionId = it.admin().contentVersions().create(exhibitionId).id!!
        val createdFloorId = it.admin().exhibitionFloors().create(exhibitionId).id!!
        val createdRoomId = it.admin().exhibitionRooms().create(exhibitionId, createdFloorId).id!!
        val createdDeviceGroupId = it.admin().exhibitionDeviceGroups().create(exhibitionId, createdRoomId).id!!

        val groupContentVersionToCreate = GroupContentVersion(
            name = "Group name",
            status = GroupContentVersionStatus.notstarted,
            deviceGroupId = createdDeviceGroupId,
            contentVersionId = createContentVersionId
        )

        val createdGroupContentVersion = it.admin().groupContentVersions().create(exhibitionId, groupContentVersionToCreate)
        assertNotNull(createdGroupContentVersion)
        it.admin().exhibitions().assertCreateFail(400, "")
      }
  }

  @Test
  fun testUpdateGroupContentVersion() {
    ApiTestBuilder().use {
      val exhibitionId = it.admin().exhibitions().create().id!!
      val createdContentVersionId = it.admin().contentVersions().create(exhibitionId).id!!
      val createdFloorId = it.admin().exhibitionFloors().create(exhibitionId).id!!
      val createdRoomId = it.admin().exhibitionRooms().create(exhibitionId, createdFloorId).id!!
      val createdDeviceGroupId = it.admin().exhibitionDeviceGroups().create(exhibitionId, createdRoomId).id!!

      val groupContentVersionToCreate = GroupContentVersion(
        name = "Group name",
        status = GroupContentVersionStatus.notstarted,
        deviceGroupId = createdDeviceGroupId,
        contentVersionId = createdContentVersionId
      )

      val createdGroupContentVersion = it.admin().groupContentVersions().create(exhibitionId, groupContentVersionToCreate)
      assertNotNull(createdGroupContentVersion)

      val groupContentVersionToUpdate = GroupContentVersion(
        id = createdGroupContentVersion.id!!,
        name = "Updated name",
        status = GroupContentVersionStatus.inprogress,
        deviceGroupId = createdDeviceGroupId,
        contentVersionId = createdContentVersionId
      )

      val updatedGroupContentVersion = it.admin().groupContentVersions().updateGroupContentVersion(exhibitionId, groupContentVersionToUpdate)
      assertNotNull(updatedGroupContentVersion)
      assertEquals(createdGroupContentVersion.id!!, updatedGroupContentVersion?.id!!)
      assertEquals(createdGroupContentVersion.contentVersionId, updatedGroupContentVersion.contentVersionId)
      assertEquals(createdGroupContentVersion.deviceGroupId, updatedGroupContentVersion.deviceGroupId)
      assertNotEquals(createdGroupContentVersion.status, updatedGroupContentVersion.status)
      assertEquals(GroupContentVersionStatus.inprogress, updatedGroupContentVersion.status)
    }
  }

  @Test
  fun testFindGroupContentVersion() {
    ApiTestBuilder().use {
      val exhibitionId = it.admin().exhibitions().create().id!!
      val nonExistingExhibitionId = UUID.randomUUID()
      val nonExistingGroupContentVersionId = UUID.randomUUID()

      val createdContentVersionId = it.admin().contentVersions().create(exhibitionId).id!!
      val createdFloorId = it.admin().exhibitionFloors().create(exhibitionId).id!!
      val createdRoomId = it.admin().exhibitionRooms().create(exhibitionId, createdFloorId).id!!
      val createdDeviceGroupId = it.admin().exhibitionDeviceGroups().create(exhibitionId, createdRoomId).id!!

      val groupContentVersionToCreate = GroupContentVersion(
        name = "Group name",
        status = GroupContentVersionStatus.notstarted,
        deviceGroupId = createdDeviceGroupId,
        contentVersionId = createdContentVersionId
      )

      val createdGroupContentVersion = it.admin().groupContentVersions().create(exhibitionId, groupContentVersionToCreate)

      it.admin().groupContentVersions().assertFindFail(404, exhibitionId, nonExistingGroupContentVersionId)
      it.admin().groupContentVersions().assertFindFail(404, nonExistingExhibitionId, nonExistingGroupContentVersionId)
      it.admin().groupContentVersions().assertFindFail(404, nonExistingExhibitionId, createdContentVersionId)
      assertNotNull(it.admin().groupContentVersions().findGroupContentVersion(exhibitionId, createdGroupContentVersion.id!!))
    }
  }

  @Test
  fun testListGroupContentVersions() {
      ApiTestBuilder().use {
        val exhibitionId = it.admin().exhibitions().create().id!!
        val nonExistingExhibitionId = UUID.randomUUID()

        it.admin().groupContentVersions().assertListFail(404, nonExistingExhibitionId)
        assertEquals(0, it.admin().groupContentVersions().listGroupContentVersions(exhibitionId).size)


        val createdContentVersionId = it.admin().contentVersions().create(exhibitionId).id!!
        val createdFloorId = it.admin().exhibitionFloors().create(exhibitionId).id!!
        val createdRoomId = it.admin().exhibitionRooms().create(exhibitionId, createdFloorId).id!!
        val createdDeviceGroupId = it.admin().exhibitionDeviceGroups().create(exhibitionId, createdRoomId).id!!

        val groupContentVersionToCreate = GroupContentVersion(
          name = "Group name",
          status = GroupContentVersionStatus.notstarted,
          deviceGroupId = createdDeviceGroupId,
          contentVersionId = createdContentVersionId
        )

        val createdGroupContentVersion = it.admin().groupContentVersions().create(exhibitionId, groupContentVersionToCreate)
        val groupContentVersions = it.admin().groupContentVersions().listGroupContentVersions(exhibitionId)
        assertEquals(1, groupContentVersions.size)
        assertEquals(createdGroupContentVersion.id!!, groupContentVersions[0].id)
        it.admin().groupContentVersions().delete(exhibitionId, createdGroupContentVersion.id!!)
        assertEquals(0, it.admin().groupContentVersions().listGroupContentVersions(exhibitionId).size)
      }
  }

}