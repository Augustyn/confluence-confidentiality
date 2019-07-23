package ch.nine.confluence.confidentiality.service

import ch.nine.confluence.confidentiality.repository.ConfidentialityRepository
import ch.nine.confluence.confidentiality.api.model.Confidentiality
import ch.nine.confluence.confidentiality.auditlog.AuditLogger
import ch.nine.confluence.confidentiality.repository.SpacePropertyRepository
import com.atlassian.confluence.pages.Page
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.log4j.LogManager
import java.util.Optional.ofNullable

/**
 * Class responsible for managing confidentiality response per page.
 * Checks user permissions, and sends confidentiality changed events.
 */
class ConfidentialityService constructor(private val pageRepository: ConfidentialityRepository,
                                         private val spaceRepository: SpacePropertyRepository,
                                         private val logger: AuditLogger,
                                         private val permissionService: PermissionService) {
    companion object {
        private val log = LogManager.getLogger(this::class.java.name.substringBefore("\$Companion"))
    }

    fun getConfidentiality(page: Page): Confidentiality {
        val enabled = spaceRepository.isConfidentialityEnabled(page.space.key)
        if (!enabled) {
            return Confidentiality(enabled, null, null, null)
        }
        val confidentiality = pageRepository.getConfidentiality(page)
        val confidentialityOptions = spaceRepository.getSpaceConfidentialityOptions(page.space.key).map { it.getConfidentiality() }
        return Confidentiality(enabled, confidentiality, confidentialityOptions, permissionService.canUserEdit(page))
    }

    fun saveConfidentiality(page: Page, newConfidentiality: String): Confidentiality {
        val enabled = spaceRepository.isConfidentialityEnabled(page.space.key)
        if (!enabled) {
            return Confidentiality(enabled, null, null, null)
        }
        val oldConfidentiality = pageRepository.getConfidentiality(page)
        val confidentiality = pageRepository.save(page, newConfidentiality)
        val confidentialityOptions = spaceRepository.getSpaceConfidentialityOptions(page.space.key).map { it.getConfidentiality() }

        auditLog(oldConfidentiality, newConfidentiality, page)
        return Confidentiality(enabled, confidentiality, confidentialityOptions, permissionService.canUserEdit(page))
    }

    fun validateConfidentiality(page: Page, newConfidentiality: String): Boolean {
        return newConfidentiality in spaceRepository.getSpaceConfidentialityOptions(page.space.key).map { it.getConfidentiality() }
    }

    fun isConfidentialityEnabled(page:Page): Boolean {
        return spaceRepository.isConfidentialityEnabled(page.space.key)
    }

    private fun auditLog(old: String, new: String, page: Page) {
        val user = ofNullable(AuthenticatedUserThreadLocal.get()).orElseThrow { RuntimeException("User cannot be unauthenticated!") }
        val change = ImmutablePair.of(old, new)
        log.info("User: ${user.name}, ${user.fullName} is changing confidentiality for page id: ${page.id}, change: $change")
        logger.confidentialityChanged(page, change, user, permissionService.isSystemAdministrator(user))
    }

    fun canUserEdit(page: Page?): Boolean {
        return permissionService.canUserEdit(page)
    }

    fun canUserView(page: Page?): Boolean {
        return permissionService.canUserView(page)
    }


}