package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.ExhibitionsApi
import fi.metatavu.noheva.api.spec.model.Exhibition
import fi.metatavu.noheva.api.translate.ExhibitionTranslator
import fi.metatavu.noheva.contents.ContentVersionController
import fi.metatavu.noheva.exhibitions.ExhibitionController
import fi.metatavu.noheva.utils.IdMapper
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.stream.Collectors
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Exhibitions api implementation
 */
@RequestScoped
@Transactional
class ExhibitionsApiImpl : ExhibitionsApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var exhibitionTranslator: ExhibitionTranslator

    @Inject
    lateinit var contentVersionController: ContentVersionController

    override fun listExhibitions(): Response {
        val result = exhibitionController.listExhibitions().stream().map {
            exhibitionTranslator.translate(it)
        }.collect(Collectors.toList())

        return createOk(result)
    }

    override fun createExhibition(sourceExhibitionId: UUID?, exhibition: Exhibition?): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)

        val result = if (sourceExhibitionId != null) {
            val sourceExhibition = exhibitionController.findExhibitionById(sourceExhibitionId) ?: return createNotFound(
                "Source exhibition $sourceExhibitionId not found"
            )

            val idMapper = IdMapper()

            exhibitionController.copyExhibition(
                idMapper = idMapper,
                sourceExhibition = sourceExhibition,
                creatorId = userId
            )
        } else {
            if (exhibition == null) {
                return createBadRequest(MISSING_REQUEST_BODY)
            }

            if (StringUtils.isBlank(exhibition.name)) {
                return createBadRequest("Missing exhibition name")
            }

            exhibitionController.createExhibition(
                name = exhibition.name,
                creatorId = userId
            )
        }

        return createOk(exhibitionTranslator.translate(result))
    }

    override fun findExhibition(exhibitionId: UUID): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        return createOk(exhibitionTranslator.translate(exhibition))
    }

    override fun updateExhibition(exhibitionId: UUID, exhibition: Exhibition): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)

        if (StringUtils.isBlank(exhibition.name)) {
            return createBadRequest("Missing exhibition name")
        }

        val foundExhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val updatedExhibition = exhibitionController.updateExhibition(foundExhibition, exhibition.name, userId)

        return createOk(exhibitionTranslator.translate(updatedExhibition))
    }

    override fun deleteExhibition(exhibitionId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        val contentVersions =
            contentVersionController.listContentVersions(exhibition = exhibition, exhibitionRoom = null, deviceGroup = null)
        if (contentVersions.isNotEmpty()) {
            val contentVersionIds = contentVersions.map { it.id }.joinToString()
            return createBadRequest("Cannot delete exhibition $exhibitionId because it's used in content versions $contentVersionIds")
        }

        exhibitionController.deleteExhibition(exhibition)

        return createNoContent()
    }

}
