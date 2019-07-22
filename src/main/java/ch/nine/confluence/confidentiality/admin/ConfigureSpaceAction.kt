package ch.nine.confluence.confidentiality.admin

import ch.nine.confluence.confidentiality.service.SpaceConfidentialityService
import com.atlassian.confluence.security.Permission.ADMINISTER
import com.atlassian.confluence.spaces.actions.SpaceAdminAction
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import org.apache.log4j.LogManager

/**
 * Class is responsible for populating {@link configure-space-confidentiality.vm}
 *
 * Due atlassian requirements (https://jira.atlassian.com/browse/CONFSERVER-15011) space actions requires non-argument constructor.
 * Dependency injection should be through setter methods, but it's not default in plugins version 2; Keep in mind appropriate
 * configuration in spring/plugin-context.xml (both constructor injection and setter, as fallback).
 */
class ConfigureSpaceAction: SpaceAdminAction() {
    private lateinit var administerService: SpaceConfidentialityService

    companion object {
        private val log = LogManager.getLogger(this::class.java.name.substringBefore("\$Companion"))
    }

    override fun doDefault(): String {
        val user = AuthenticatedUserThreadLocal.get()
        val isAdmin = permissionManager.hasPermission(user, ADMINISTER, space)
        log.info("User: $user is opening configure confidentiality for space: $key. Have permission to do so? $isAdmin")
        return if (isAdmin) INPUT else "error"
    }

    fun confidentialityEnabled(): Boolean {
        return administerService.getConfidentiality(space).getEnabled()
    }

    fun confidentialityLevels(): List<String> {
        return administerService.getConfidentialityOptions(space).map { it.getConfidentiality() }
    }

    // dependency injection must be done via setter methods
    fun setAdministrerConfidentialityService(administerService: SpaceConfidentialityService) {
        this.administerService = administerService
    }

}