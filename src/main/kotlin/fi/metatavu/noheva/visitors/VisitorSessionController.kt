package fi.metatavu.noheva.visitors

import fi.metatavu.noheva.api.spec.model.VisitorSessionState
import fi.metatavu.noheva.api.spec.model.VisitorSessionVariable
import fi.metatavu.noheva.api.spec.model.VisitorSessionVisitedDeviceGroup
import fi.metatavu.noheva.api.spec.model.VisitorVariableType
import fi.metatavu.noheva.persistence.dao.*
import fi.metatavu.noheva.persistence.model.Exhibition
import fi.metatavu.noheva.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.noheva.persistence.model.Visitor
import fi.metatavu.noheva.persistence.model.VisitorSession
import fi.metatavu.noheva.settings.SettingsController
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for visitor sessions
 */
@ApplicationScoped
class VisitorSessionController {

    @Inject
    lateinit var settingsController: SettingsController

    @Inject
    lateinit var visitorVariableController: VisitorVariableController

    @Inject
    lateinit var visitorDAO: VisitorDAO

    @Inject
    lateinit var visitorSessionDAO: VisitorSessionDAO

    @Inject
    lateinit var visitorSessionVariableDAO: VisitorSessionVariableDAO

    @Inject
    lateinit var visitorSessionVisitorDAO: VisitorSessionVisitorDAO

    @Inject
    lateinit var visitorSessionVisitedDeviceGroupDAO: VisitorSessionVisitedDeviceGroupDAO

    /**
     * Creates a new visitor session
     *
     * @param exhibition exhibition
     * @param state state
     * @param language language
     * @param creatorId creator id
     * @return created visitor session
     */
    fun createVisitorSession(
        exhibition: Exhibition,
        state: VisitorSessionState,
        language: String,
        creatorId: UUID
    ): VisitorSession {
        val expiresAt = OffsetDateTime.now().plus(settingsController.getVisitorSessionTimeout())

        return visitorSessionDAO.create(
            id = UUID.randomUUID(),
            exhibition = exhibition,
            state = state,
            language = language,
            expiresAt = expiresAt,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Finds a visitor session by id. Method returns null for deprecated visitor sessions
     *
     * @param id id
     * @return visitor session or null if not found
     */
    fun findVisitorSessionById(id: UUID): VisitorSession? {
        return visitorSessionDAO.findById(id = id)
    }

    /**
     * Lists visitor sessions. Method only lists visitor sessions that are still valid (not deprecated)
     *
     * @param exhibition filter by exhibition
     * @param modifiedAfter filter visitor sessions modified after specified time
     * @param tagId filter by tagId
     * @return list of visitor sessions
     */
    fun listVisitorSessions(
        exhibition: Exhibition,
        modifiedAfter: OffsetDateTime?,
        tagId: String?
    ): List<VisitorSession> {
        var visitor: Visitor? = null

        if (tagId != null) {
            visitor = visitorDAO.findByExhibitionAndTagId(exhibition = exhibition, tagId = tagId)
            visitor ?: return listOf()
        }

        if (visitor != null) {
            return visitorSessionVisitorDAO.listSessionsByVisitor(visitor = visitor)
        }

        return visitorSessionDAO.list(
            exhibition = exhibition,
            modifiedAfter = modifiedAfter,
            expiresAfter = OffsetDateTime.now()
        )
    }

    /**
     * Updates visitor session
     *
     * @param visitorSession visitor session to be updated
     * @param state new state
     * @param language language
     * @param lastModfierId modifier user id
     * @return updated visitor session
     */
    fun updateVisitorSession(visitorSession: VisitorSession, state: VisitorSessionState, language: String, lastModfierId: UUID): VisitorSession {
        var result = visitorSessionDAO.updateState(visitorSession, state, lastModfierId)
        result = visitorSessionDAO.updateLanguage(result, language, lastModfierId)
        return result
    }

    /**
     * Sets visitor session visitors
     *
     * @param visitorSession visitor session
     * @param visitors visitors
     * @return whether user list has changed or not
     */
    fun setVisitorSessionVisitors(visitorSession: VisitorSession, visitors: List<Visitor>): Boolean {
        var changed = false
        val existingSessionVisitors = visitorSessionVisitorDAO.listByVisitorSession(visitorSession).toMutableList()

        for (visitor in visitors) {
            val existingSessionVisitor = existingSessionVisitors.find { it.visitor?.id == visitor.id }
            if (existingSessionVisitor == null) {
                visitorSessionVisitorDAO.create(UUID.randomUUID(), visitorSession, visitor)
                changed = true
            } else {
                existingSessionVisitors.remove(existingSessionVisitor)
            }
        }

        changed = changed || existingSessionVisitors.isNotEmpty()

        existingSessionVisitors.forEach(visitorSessionVisitorDAO::delete)

        return changed
    }

    /**
     * Returns whether visitor session variable is valid or not
     *
     * @param exhibition exhibition
     * @param visitorSessionVariable visitor session variable
     * @return whether visitor session variable is valid or not
     */
    fun isValidVisitorSessionVariable(exhibition: Exhibition, visitorSessionVariable: VisitorSessionVariable): Boolean {
        val value = visitorSessionVariable.value
        if (value.isNullOrEmpty()) {
            return true
        }

        val visitorVariable = visitorVariableController.findVisitorVariableByExhibitionAndName(exhibition = exhibition, name = visitorSessionVariable.name)
        visitorVariable ?: return false

        when (visitorVariable.type) {
            VisitorVariableType.BOOLEAN -> return BooleanUtils.toBooleanObject(value) != null
            VisitorVariableType.NUMBER -> return NumberUtils.isParsable(value)
            VisitorVariableType.TEXT -> return true
            VisitorVariableType.ENUMERATED -> return visitorVariable.enum?.contains(visitorSessionVariable.value, false) ?: false
        }

        return false
    }

    /**
     * Sets visitor session variables
     *
     * @param visitorSession visitor session
     * @param variables variables
     * @return whether session variable list has changed or not
     */
    fun setVisitorSessionVariables(visitorSession: VisitorSession, variables: List<VisitorSessionVariable>): Boolean {
        var changed = false
        val existingSessionVariables = visitorSessionVariableDAO.listByVisitorSession(visitorSession).toMutableList()

        for (variable in variables) {
            val existingSessionVariable = existingSessionVariables.find { it.name == variable.name }
            if (existingSessionVariable == null) {
                visitorSessionVariableDAO.create(UUID.randomUUID(), visitorSession, variable.name, variable.value!!)
                changed = true
            } else {
                if (!StringUtils.isBlank(variable.value)) {
                    if (variable.value != existingSessionVariable.value) {
                        visitorSessionVariableDAO.updateValue(existingSessionVariable, variable.value!!)
                        changed = true
                    }

                    existingSessionVariables.remove(existingSessionVariable)
                }
            }
        }

        changed = changed || existingSessionVariables.isNotEmpty()
        existingSessionVariables.forEach(visitorSessionVariableDAO::delete)

        return changed
    }

    /**
     * Sets visitor session visited device groups
     *
     * @param visitorSession visitor session
     * @param visitedDeviceGroups visited device groups
     * @return whether visited device list has changed or not
     */
    fun setVisitorSessionVisitedDeviceGroups(visitorSession: VisitorSession, visitedDeviceGroups: List<VisitorSessionVisitedDeviceGroup>, visitedDeviceGroupList: List<ExhibitionDeviceGroup>) {
        val existingSessionVisitedDeviceGroups = visitorSessionVisitedDeviceGroupDAO.listByVisitorSession(visitorSession).toMutableList()

        for (visitedDeviceGroup in visitedDeviceGroups) {
            val existingSessionVisitedDeviceGroup = existingSessionVisitedDeviceGroups.find { it.deviceGroup?.id == visitedDeviceGroup.deviceGroupId }
            if (existingSessionVisitedDeviceGroup == null) {
                visitorSessionVisitedDeviceGroupDAO.create(
                    id = UUID.randomUUID(),
                    visitorSession = visitorSession,
                    deviceGroup = visitedDeviceGroupList.first{ it.id == visitedDeviceGroup.deviceGroupId },
                    enteredAt = visitedDeviceGroup.enteredAt!!,
                    exitedAt = visitedDeviceGroup.exitedAt!!
                )
            } else {
                visitorSessionVisitedDeviceGroupDAO.updateEnteredAt(existingSessionVisitedDeviceGroup, visitedDeviceGroup.enteredAt!!)
                visitorSessionVisitedDeviceGroupDAO.updateExitedAt(existingSessionVisitedDeviceGroup, visitedDeviceGroup.exitedAt!!)
                existingSessionVisitedDeviceGroups.remove(existingSessionVisitedDeviceGroup)
            }
        }

        existingSessionVisitedDeviceGroups.forEach(visitorSessionVisitedDeviceGroupDAO::delete)
    }

    /**
     * Deletes visitor session
     *
     * @param visitorSession visitor session
     */
    fun deleteVisitorSession(visitorSession: VisitorSession) {
        visitorSessionVariableDAO.listByVisitorSession(visitorSession).forEach(visitorSessionVariableDAO::delete)
        visitorSessionVisitorDAO.listByVisitorSession(visitorSession).forEach(visitorSessionVisitorDAO::delete)
        visitorSessionVisitedDeviceGroupDAO.listByVisitorSession(visitorSession).forEach(visitorSessionVisitedDeviceGroupDAO::delete)
        visitorSessionDAO.delete(visitorSession)
    }

}
