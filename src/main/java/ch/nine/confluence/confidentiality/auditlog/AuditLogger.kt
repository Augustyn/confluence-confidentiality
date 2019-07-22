package ch.nine.confluence.confidentiality.auditlog

import ch.nine.confluence.confidentiality.admin.model.AdministerConfidentialityRow
import com.atlassian.confluence.api.model.audit.AffectedObject
import com.atlassian.confluence.api.model.audit.AuditRecord
import com.atlassian.confluence.api.model.people.User
import com.atlassian.confluence.api.service.audit.AuditService
import com.atlassian.confluence.pages.Page
import com.atlassian.confluence.spaces.Space
import com.atlassian.confluence.user.ConfluenceUser
import com.opensymphony.webwork.ServletActionContext
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.log4j.LogManager
import org.joda.time.DateTime
import javax.servlet.http.HttpServletRequest

/**
 * Class responsible for persisting changes of Confidentiality. Creates audit log event, to be stored by confluence.
 * Audit logs, can be view by confluence administrator in Audit Log section of confluence administration.
 */
class AuditLogger constructor(private val storage: AuditService) {
    companion object {
        private val log = LogManager.getLogger(this::class.java.name.substringBefore("\$Companion"))
    }

    fun confidentialityChanged(page: Page, change: ImmutablePair<String, String>, byUser: ConfluenceUser, isSysAdm: Boolean) {
        val logDescription = "Confidentiality for page: ${page.id}, space: ${page.spaceKey}, title: '${page.title}', changed from: '${change.left}' to: '${change.right}' , by: ${byUser.name}, ${byUser.fullName}, system admin? $isSysAdm"
        log.info(logDescription)
        val summary = "Page id: ${page.id} confidentiality change: $change}"
        val auditRecord = buildAuditRecord(mapToAffectedObject(page), logDescription, summary, byUser, isSysAdm)
        storage.storeRecord(auditRecord)
    }

    fun confidentialitySaved(space: Space, list: List<AdministerConfidentialityRow>, user: ConfluenceUser, isAdmin: Boolean) {
        val longDescription = "Confidentiality for space $space saved by user: $user, admin? $isAdmin. New confidentiality options: ${list.joinToString { " " }}"
        log.info(longDescription)
        val summary = "New confidentiality list for space: $space"
        val auditRecord = buildAuditRecord(mapToAffectedObject(space), longDescription, summary, user, isAdmin)
        storage.storeRecord(auditRecord)
    }

    fun confidentialityChanged(space: Space, change: ImmutablePair<AdministerConfidentialityRow, AdministerConfidentialityRow>, byUser: ConfluenceUser, isSysAdm: Boolean) {
        val longDescription = "Confidentiality for space '${space.key}', id: ${space.id} changed from: '${change.left}' to: ${change.right}, by user: $byUser. Sys adm? $isSysAdm"
        log.info(longDescription)
        val summary = "Space id: ${space.id} confidentiality changed"
        val auditRecord = buildAuditRecord(mapToAffectedObject(space), longDescription, summary, byUser, isSysAdm)
        storage.storeRecord(auditRecord)
    }

    fun confidentialityRemoved(space: Space, row: AdministerConfidentialityRow?, user: ConfluenceUser, isSysAdm: Boolean) {
        val longDescription = "Confidentiality $row removed for space '$space', by user: by user: $user. Sys adm? $isSysAdm"
        log.info(longDescription)
        val summary = "Space id: $space confidentiality id ${row?.getId()} removed"
        val auditRecord = buildAuditRecord(mapToAffectedObject(space), longDescription, summary, user, isSysAdm)
        storage.storeRecord(auditRecord)
    }

    private fun buildAuditRecord(affectedObject: AffectedObject, longDescription: String, summary: String, byUser: ConfluenceUser, isSysAdm: Boolean): AuditRecord {
        val request: HttpServletRequest? = ServletActionContext.getRequest()

        return AuditRecord.Builder()
                .affectedObject(affectedObject)
                .category("change")
                .createdDate(DateTime.now())
                .description(longDescription)
                .summary(summary)
                .author(mapUser(byUser))
                .isSysAdmin(isSysAdm)
                .remoteAddress(request?.getHeader("X-Forwarded-For") ?: request?.remoteAddr ?: "remote unknown")
                .build()
    }

    private fun mapToAffectedObject(page: Page): AffectedObject {
        return affectedObjectBuilder("Page id: ${page.id}, space: ${page.spaceKey}", page.displayTitle)
    }

    private fun mapToAffectedObject(space: Space): AffectedObject {
        return affectedObjectBuilder("Space id: ${space.id}, key: ${space.key}", space.key)
    }

    private fun affectedObjectBuilder(name: String, type: String): AffectedObject {
        return AffectedObject
                .builder()
                .name(name)
                .objectType(type)
                .build()
    }

    private fun mapUser(user: ConfluenceUser) =
            User(null, user.name, user.fullName, user.key)

}
