package ch.nine.confluence.confidentiality.service


import ch.nine.confluence.confidentiality.admin.model.AdministerConfidentiality
import ch.nine.confluence.confidentiality.admin.model.AdministerConfidentialityRow
import ch.nine.confluence.confidentiality.auditlog.AuditLogger
import ch.nine.confluence.confidentiality.repository.SpacePropertyRepository
import com.atlassian.confluence.spaces.Space
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.log4j.LogManager
import java.lang.IllegalStateException
import java.util.Optional.ofNullable
/**
 * Class responsible for configuring confidentiality by space administrator
 * Checks user permissions, and sends confidentiality changed events.
 */
class SpaceConfidentialityService constructor(private val repository: SpacePropertyRepository,
                                              private val permissionService: PermissionService,
                                              private val logger: AuditLogger) {
    companion object {
        private val log = LogManager.getLogger(this::class.java.name.substringBefore("\$Companion"))
        private const val unauthenticated = "User cannot be unauthenticated!"
    }

    fun getConfidentiality(space: Space): AdministerConfidentiality {
        return repository.getSpaceConfidentialityProperty(space.key)
    }

    fun getConfidentialityOptions(space: Space): List<AdministerConfidentialityRow> {
        return repository.getSpaceConfidentialityOptions(space.key)
    }

    fun saveConfidentiality(space: Space, confidentiality: AdministerConfidentialityRow): AdministerConfidentialityRow {
        val options = repository.getSpaceConfidentialityOptions(space.key) as MutableList<AdministerConfidentialityRow>
        options.add(AdministerConfidentialityRow(countRowId(options), normalizeConfidentiality(confidentiality.getConfidentiality())))
        auditLog(listOf(confidentiality), space)
        return repository.storeProperty(space.key, options).first{ it.getId() == confidentiality.getId() }
    }

    fun updateConfidentiality(space: Space, id: Int, confidentiality: AdministerConfidentialityRow): AdministerConfidentialityRow {
        if (id != confidentiality.getId()) {
            throw IllegalStateException("Cannot save confidentiality. IDs mismatch: path variable ($id) vs object id (${confidentiality.getId()}")
        }
        val options = repository.getSpaceConfidentialityOptions(space.key) as MutableList<AdministerConfidentialityRow>
        val idx = options.indexOfFirst { it.getId() == confidentiality.getId() }
        val old = options[idx]
        val new = AdministerConfidentialityRow(old.getId(), normalizeConfidentiality(confidentiality.getConfidentiality()))
        options[idx] = new
        return repository.storeProperty(space.key, options).first { it.getId() == id }
    }

    private fun countRowId(options: MutableList<AdministerConfidentialityRow>): Int {
        val maxIdRow = options.maxWith(Comparator.comparingInt { it.getId() })
        return maxOf((maxIdRow?.getId() ?: 0), options.size + 1)
    }

    private fun normalizeConfidentiality(confidentiality: String) =
            // characters # and , are separators for storing confidentiality option. They cannot be used as the value
            confidentiality.toLowerCase().replace("#", "").replace(",", "")

    private fun auditLog(list: List<AdministerConfidentialityRow>, space: Space) {
        val user = ofNullable(AuthenticatedUserThreadLocal.get()).orElseThrow { RuntimeException(unauthenticated) }
        logger.confidentialitySaved(space, list, user, permissionService.isSystemAdministrator(user))
    }

    fun removeConfidentiality(space: Space, id: Int): Boolean {
        val user = ofNullable(AuthenticatedUserThreadLocal.get()).orElseThrow { RuntimeException(unauthenticated) }
        log.info("User $user is removing confidentiality option: $id, from space key: $space")
        val options = getConfidentialityOptions(space)
        logger.confidentialityRemoved(space, options.find { it.getId() == id }, user, permissionService.isSystemAdministrator(user))

        repository.storeProperty(space.key, options.filter { it.getId() != id })
        return true
    }

}