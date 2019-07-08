package ch.nine.confluence.confidentiality.service


import ch.nine.confluence.confidentiality.admin.model.AdministerConfidentiality
import ch.nine.confluence.confidentiality.admin.model.AdministerConfidentialityRow
import ch.nine.confluence.confidentiality.auditlog.AuditLogger
import ch.nine.confluence.confidentiality.repository.AdminPropertyRepository
import com.atlassian.confluence.spaces.Space
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.log4j.LogManager
import java.util.Optional.ofNullable
/**
 * Class responsible for configuring confidentiality by space administrator
 * Checks user permissions, and sends confidentiality changed events.
 */
class AdministrerConfidentialityService constructor(private val repository: AdminPropertyRepository,
                                                    private val permissionService: PermissionService,
                                                    private val logger: AuditLogger) {
    companion object {
        private val log = LogManager.getLogger(this::class.java.name.substringBefore("\$Companion"))
    }

    fun getConfidentiality(space: Space): AdministerConfidentiality {
        return repository.getSpaceConfidentiality(space.key)
    }

    fun getConfidentialityOptions(space: Space): List<AdministerConfidentialityRow> {
        val options = repository.getSpaceConfidentialityOptions(space.key)
        return options.mapIndexed{ idx, option -> AdministerConfidentialityRow(idx, option)}
    }

    fun saveConfidentiality(space: Space, enabled: Boolean, newConfidentialityList: List<String>): AdministerConfidentiality {
        val oldConfidentiality = repository.getSpaceConfidentiality(space.key)
        val newOptions = repository.storeProperty(space.key, AdministerConfidentiality(enabled, newConfidentialityList))
        auditLog(oldConfidentiality, newOptions, space)
        return newOptions
    }

    fun saveConfidentiality(space: Space, confidentiality: AdministerConfidentialityRow): AdministerConfidentialityRow {
//        val optionList = repository.storeProperty(space.key, confidentiality) //TODO: fixme;
        return AdministerConfidentialityRow(confidentiality.getId(), confidentiality.getConfidentiality())
    }

    fun addConfidentiality(space: Space, confidentiality: String): Any? {
        val oldConfidentialityOptions = repository.getSpaceConfidentiality(space.key)
        val confidentialityList = oldConfidentialityOptions.getConfidentiality().filter { it == confidentiality } as MutableList<String>
        confidentialityList.add(confidentiality)
        val storedConfidentiality = repository.storeProperty(space.key, AdministerConfidentiality(/*oldConfidentialityOptions.getEnabled()*/true, confidentialityList))
        auditLog(oldConfidentialityOptions, storedConfidentiality, space)
        return storedConfidentiality
    }

    private fun auditLog(old: AdministerConfidentiality, new: AdministerConfidentiality, space: Space) {
        val user = ofNullable(AuthenticatedUserThreadLocal.get()).orElseThrow { RuntimeException("User cannot be unauthenticated!") }
        val change = ImmutablePair.of(old, new)
        log.info("User: ${user.name}, ${user.fullName} is changing default confidentiality for space key: ${space.key}, change: $change")
        logger.confidentialityChanged(space, change, user, permissionService.isSystemAdministrator(user))
    }

    fun removeConfidentiality(space: Space, id: String, confidentiality: String): Boolean {
        val user = ofNullable(AuthenticatedUserThreadLocal.get()).orElseThrow { RuntimeException("User cannot be unauthenticated!") }
        log.info("User $user is removing confidentiality option: $id, or $confidentiality, from space key: $space")
        return true
    }

}