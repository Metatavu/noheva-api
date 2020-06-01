package fi.metatavu.muisti.visitors

import fi.metatavu.muisti.api.spec.model.VisitorSessionState
import fi.metatavu.muisti.api.spec.model.VisitorSessionVariable
import fi.metatavu.muisti.api.spec.model.VisitorSessionVisitedDeviceGroup
import fi.metatavu.muisti.persistence.dao.*
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.muisti.persistence.model.Visitor
import fi.metatavu.muisti.persistence.model.VisitorSession
import org.apache.commons.lang3.StringUtils
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for visitor sessions
 */
@ApplicationScoped
class VisitorSessionController {

    @Inject
    private lateinit var visitorDAO: VisitorDAO

    @Inject
    private lateinit var visitorSessionDAO: VisitorSessionDAO

    @Inject
    private lateinit var visitorSessionVariableDAO: VisitorSessionVariableDAO

    @Inject
    private lateinit var visitorSessionVisitorDAO: VisitorSessionVisitorDAO

    @Inject
    private lateinit var visitorSessionVisitedDeviceGroupDAO: VisitorSessionVisitedDeviceGroupDAO

    /**
     * Creates new visitor session
     */
    fun createVisitorSession(exhibition: Exhibition, state: VisitorSessionState, creatorId: UUID): VisitorSession {
        return visitorSessionDAO.create(UUID.randomUUID(), exhibition, state, creatorId, creatorId)
    }

    /**
     * Finds a visitor session by id
     *
     * @param id id
     * @return visitor session id
     */
    fun findVisitorSessionById(id: UUID): VisitorSession? {
        return visitorSessionDAO.findById(id)
    }

    /**
     * Lists visitor sessions
     *
     * @param exhibition filter by exhibition
     * @param tagId filter by tagId
     * @return list of visitor sessions
     */
    fun listVisitorSessions(exhibition: Exhibition, tagId: String?): List<VisitorSession> {
        var visitor: Visitor? = null

        if (tagId != null) {
            visitor = visitorDAO.findByExhibitionAndTagId(exhibition = exhibition, tagId = tagId)
            visitor ?: return listOf()
        }

        if (visitor != null) {
            return visitorSessionVisitorDAO.listSessionsByVisitor(visitor = visitor)
        }

        return visitorSessionDAO.list(exhibition = exhibition)
    }

    /**
     * Updates visitor session
     *
     * @param visitorSession visitor session to be updated
     * @param state new state
     * @param lastModfierId modifier user id
     * @return updated visitor session
     */
    fun updateVisitorSession(visitorSession: VisitorSession, state: VisitorSessionState, lastModfierId: UUID): VisitorSession {
        return visitorSessionDAO.updateState(visitorSession, state, lastModfierId)
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

        changed = changed || !existingSessionVisitors.isEmpty()

        existingSessionVisitors.forEach(visitorSessionVisitorDAO::delete)

        return changed
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
                visitorSessionVariableDAO.create(UUID.randomUUID(), visitorSession, variable.name, variable.value)
                changed = true
            } else {
                if (!StringUtils.isBlank(variable.value)) {
                    if (variable.value.equals(existingSessionVariable.value)) {
                        visitorSessionVariableDAO.updateValue(existingSessionVariable, variable.value)
                        changed = true
                    }

                    existingSessionVariables.remove(existingSessionVariable)
                }
            }
        }

        changed = changed || !existingSessionVariables.isEmpty()
        existingSessionVariables.forEach(visitorSessionVariableDAO::delete)

        return changed
    }

    /**
     * Sets visitor session visited device groups
     *
     * @param visitorSession visitor session
     * @param visitedDeviceGroups device groups
     * @return whether user list has changed or not
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
                    enteredAt = visitedDeviceGroup.enteredAt,
                    exitedAt = visitedDeviceGroup.exitedAt
                )
            } else {
                visitorSessionVisitedDeviceGroupDAO.updateEnteredAt(existingSessionVisitedDeviceGroup, visitedDeviceGroup.enteredAt)
                visitorSessionVisitedDeviceGroupDAO.updateExitedAt(existingSessionVisitedDeviceGroup, visitedDeviceGroup.exitedAt)
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
