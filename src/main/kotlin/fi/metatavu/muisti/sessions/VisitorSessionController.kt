package fi.metatavu.muisti.sessions

import fi.metatavu.muisti.api.spec.model.VisitorSessionState
import fi.metatavu.muisti.api.spec.model.VisitorSessionUser
import fi.metatavu.muisti.api.spec.model.VisitorSessionVariable
import fi.metatavu.muisti.persistence.dao.VisitorSessionDAO
import fi.metatavu.muisti.persistence.dao.VisitorSessionUserDAO
import fi.metatavu.muisti.persistence.dao.VisitorSessionVariableDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.VisitorSession
import org.apache.commons.lang3.StringUtils
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for visitor sessions
 */
@ApplicationScoped
open class VisitorSessionController {

    @Inject
    private lateinit var visitorSessionDAO: VisitorSessionDAO

    @Inject
    private lateinit var visitorSessionVariableDAO: VisitorSessionVariableDAO

    @Inject
    private lateinit var visitorSessionUserDAO: VisitorSessionUserDAO

    /**
     * Creates new visitor session
     */
    open fun createVisitorSession(exhibition: Exhibition, state: VisitorSessionState, creatorId: UUID): VisitorSession {
        return visitorSessionDAO.create(UUID.randomUUID(), exhibition, state, creatorId, creatorId)
    }

    /**
     * Finds a visitor session by id
     *
     * @param id id
     * @return visitor session id
     */
    open  fun findVisitorSessionById(id: UUID): VisitorSession? {
        return visitorSessionDAO.findById(id)
    }

    /**
     * Lists visitor sessions by exhibition
     *
     * @return list of visitor sessions in exhibition
     */
    open fun listVisitorSessions(exhibition: Exhibition): List<VisitorSession> {
        return visitorSessionDAO.listByExhibition(exhibition)
    }

    /**
     * Updates visitor session
     *
     * @param visitorSession visitor session to be updated
     * @param state new state
     * @param lastModfierId modifier user id
     * @return updated visitor session
     */
    open fun updateVisitorSession(visitorSession: VisitorSession, state: VisitorSessionState, lastModfierId: UUID): VisitorSession {
        return visitorSessionDAO.updateState(visitorSession, state, lastModfierId)
    }

    /**
     * Sets visitor session users
     *
     * @param visitorSession visitor session
     * @param users users
     */
    open fun setVisitorSessionUsers(visitorSession: VisitorSession, users: List<VisitorSessionUser>) {
        val existingSessionUsers = visitorSessionUserDAO.listByVisitorSession(visitorSession).toMutableList()

        for (user in users) {
            val existingSessionUser = existingSessionUsers.find { it.tagId == user.tagId && it.userId == user.userId }
            if (existingSessionUser == null) {
                visitorSessionUserDAO.create(UUID.randomUUID(), visitorSession, user.userId, user.tagId)
            } else {
                existingSessionUsers.remove(existingSessionUser)
            }
        }

        existingSessionUsers.forEach(visitorSessionUserDAO::delete)
    }

    /**
     * Sets visitor session variables
     *
     * @param visitorSession visitor session
     * @param variables variables
     */
    open fun setVisitorSessionVariables(visitorSession: VisitorSession, variables: List<VisitorSessionVariable>) {
        val existingSessionVariables = visitorSessionVariableDAO.listByVisitorSession(visitorSession).toMutableList()

        for (variable in variables) {
            val existingSessionVariable = existingSessionVariables.find { it.name == variable.name }
            if (existingSessionVariable == null) {
                visitorSessionVariableDAO.create(UUID.randomUUID(), visitorSession, variable.name, variable.value)
            } else {
                if (!StringUtils.isBlank(variable.value)) {
                    visitorSessionVariableDAO.updateValue(existingSessionVariable, variable.value)
                    existingSessionVariables.remove(existingSessionVariable)
                }
            }
        }

        existingSessionVariables.forEach(visitorSessionVariableDAO::delete)
    }

    /**
     * Deletes visitor session
     *
     * @param visitorSession visitor session
     */
    open fun deleteVisitorSession(visitorSession: VisitorSession) {
        visitorSessionVariableDAO.listByVisitorSession(visitorSession).forEach(visitorSessionVariableDAO::delete)
        visitorSessionUserDAO.listByVisitorSession(visitorSession).forEach(visitorSessionUserDAO::delete)
        visitorSessionDAO.delete(visitorSession)
    }

}