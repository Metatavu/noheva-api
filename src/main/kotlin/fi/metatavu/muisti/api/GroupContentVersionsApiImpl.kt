package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.GroupContentVersionsApi
import fi.metatavu.muisti.api.spec.model.GroupContentVersion
import fi.metatavu.muisti.api.translate.GroupContentVersionTranslator
import fi.metatavu.muisti.contents.ContentVersionController
import fi.metatavu.muisti.contents.GroupContentVersionController
import fi.metatavu.muisti.devices.ExhibitionDeviceGroupController
import fi.metatavu.muisti.exhibitions.ExhibitionController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

import javax.ws.rs.core.Response

@RequestScoped
class GroupContentVersionsApiImpl: GroupContentVersionsApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var contentVersionController: ContentVersionController

    @Inject
    lateinit var exhibitionDeviceGroupController: ExhibitionDeviceGroupController

    @Inject
    lateinit var groupContentVersionController: GroupContentVersionController

    @Inject
    lateinit var groupContentVersionTranslator: GroupContentVersionTranslator

    /* V1 */
    override fun listGroupContentVersions(exhibitionId: UUID, contentVersionId: UUID?, deviceGroupId: UUID?): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")

        var contentVersion: fi.metatavu.muisti.persistence.model.ContentVersion? = null
        if (contentVersionId != null) {
            contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createBadRequest("Content version $contentVersionId not found")
        }

        var deviceGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup? = null
        if (deviceGroupId != null) {
            deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId)?: return createBadRequest("Device group $deviceGroupId not found")
        }

        val groupContentVersions = groupContentVersionController.listGroupContentVersions(exhibition = exhibition, contentVersion = contentVersion, deviceGroup = deviceGroup)

        return createOk(groupContentVersions.map (groupContentVersionTranslator::translate))
    }

    override fun createGroupContentVersion(exhibitionId: UUID, groupContentVersion: GroupContentVersion): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")

        val contentVersionId = groupContentVersion.contentVersionId
        val contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound("Content version $contentVersionId not found")

        val deviceGroupId = groupContentVersion.deviceGroupId
        val deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createBadRequest("Invalid exhibition group id $deviceGroupId")

        val name = groupContentVersion.name
        val status = groupContentVersion.status

        val result = groupContentVersionController.createGroupContentVersion(exhibition, name, status, contentVersion, deviceGroup, userId)
        return createOk(groupContentVersionTranslator.translate(result))
    }

    override fun findGroupContentVersion(exhibitionId: UUID, groupContentVersionId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val groupContentVersion = groupContentVersionController.findGroupContentVersionById(groupContentVersionId) ?: return createNotFound("Group content version $groupContentVersionId not found")

        if (!groupContentVersion.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound(GROUP_CONTENT_VERSION_NOT_FOUND)
        }

        return createOk(groupContentVersionTranslator.translate(groupContentVersion))
    }

    override fun updateGroupContentVersion(
        exhibitionId: UUID,
        groupContentVersionId: UUID,
        groupContentVersion: GroupContentVersion
    ): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val groupContentVersionFound = groupContentVersionController.findGroupContentVersionById(groupContentVersionId) ?: return createNotFound("Group content version $groupContentVersionId not found")

        val contentVersionId = groupContentVersion.contentVersionId
        val contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound("Content version $contentVersionId not found")

        val deviceGroupId = groupContentVersion.deviceGroupId
        val deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createBadRequest("Invalid exhibition group id $deviceGroupId")

        val name = groupContentVersion.name
        val status = groupContentVersion.status
        val result = groupContentVersionController.updateGroupContentVersion(groupContentVersionFound, name, status, contentVersion, deviceGroup, userId)

        return createOk(groupContentVersionTranslator.translate(result))
    }

    override fun deleteGroupContentVersion(exhibitionId: UUID, groupContentVersionId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val groupContentVersion = groupContentVersionController.findGroupContentVersionById(groupContentVersionId) ?: return createNotFound("Group content version $groupContentVersionId not found")
        groupContentVersionController.deleteGroupContentVersion(groupContentVersion)
        return createNoContent()
    }
}
