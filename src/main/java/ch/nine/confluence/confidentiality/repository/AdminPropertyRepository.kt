package ch.nine.confluence.confidentiality.repository

import ch.nine.confluence.confidentiality.admin.model.AdministerConfidentiality
import ch.nine.confluence.confidentiality.repository.ConfigurationKeys.DEFAULT_CONFIDENTIALITY_LIST
import com.atlassian.confluence.api.model.JsonString
import com.atlassian.confluence.api.model.content.JsonSpaceProperty
import com.atlassian.confluence.api.model.content.Space
import com.atlassian.confluence.api.model.content.Version
import com.atlassian.confluence.api.model.content.Version.builder
import com.atlassian.confluence.api.model.people.KnownUser
import com.atlassian.confluence.api.model.people.UnknownUser
import com.atlassian.confluence.api.model.reference.Reference
import com.atlassian.confluence.api.service.content.ContentPropertyService.MAXIMUM_VALUE_LENGTH
import com.atlassian.confluence.api.service.content.SpacePropertyService
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import org.apache.log4j.LogManager
import java.util.*

class AdminPropertyRepository constructor(private val propertyService: SpacePropertyService) {
    companion object {
        private val log = LogManager.getLogger(this::class.java.name.substringBefore("\$Companion"))
    }

    fun storeProperty(spaceKey: String, adminConfidentiality: AdministerConfidentiality): AdministerConfidentiality {
        val option = propertyService.find().withSpaceKey(spaceKey).withPropertyKey(ConfigurationKeys.CONFIDENTIALITY).fetchOne()
        return if (option.isEmpty) storeNewProperty(spaceKey, adminConfidentiality)
        else updateProperty(spaceKey, adminConfidentiality, option.get())
    }

    fun storeProperty(spaceKey: String, options: List<String>): List<String> {
        val storedOptions = propertyService.find().withSpaceKey(spaceKey).withPropertyKey(ConfigurationKeys.CONFIDENTIALITY).fetchOne()
        return if (storedOptions.isEmpty) storeNewProperty(spaceKey, options)
        else updateProperty(spaceKey, options, storedOptions.get())
    }

    private fun updateProperty(spaceKey: String, confidentiality: AdministerConfidentiality, savedConfidentiality: JsonSpaceProperty): AdministerConfidentiality {
        TODO("not implemented, yet") //To change body of created functions use File | Settings | File Templates.
    }

    private fun updateProperty(spaceKey: String, confidentiality: List<String>, savedConfidentiality: JsonSpaceProperty): List<String> {
        val newList = savedConfidentiality.value.value.split(",") as MutableList<String>
        newList.addAll(confidentiality)
        return storeNewProperty(spaceKey, newList)
    }

    private fun storeNewProperty(spaceKey: String, confidentiality: AdministerConfidentiality): AdministerConfidentiality {
        val enabledValue = toSpaceProperty(spaceKey, confidentiality.getEnabled().toString(), ConfigurationKeys.IS_CONFIDENTIALITY_ENABLED)
        val savedEnabledValue = propertyService.create(enabledValue)
        val storedConfidentialityOptions = storeNewProperty(spaceKey, confidentiality.getConfidentiality())
        return AdministerConfidentiality(savedEnabledValue.value.value.toBoolean(), storedConfidentialityOptions)
    }

    private fun storeNewProperty(spaceKey: String, confidentiality: List<String>): List<String> {
        val stringConfidentiality = confidentiality.joinToString(",")
        if (stringConfidentiality.length > MAXIMUM_VALUE_LENGTH) {
            log.warn("Cannot save confidentiality list: '$stringConfidentiality. Too long. Max value: $MAXIMUM_VALUE_LENGTH")
            //TODO: maybe it would be better to throw Exception, or validate maximum length in controller?
        }
        val spaceConfidentialityOptions = toSpaceProperty(spaceKey, stringConfidentiality, ConfigurationKeys.SPACE_CONFIDENTIALITY_LIST)
        val savedSpaceConfidentiality = propertyService.create(spaceConfidentialityOptions)
        return savedSpaceConfidentiality.value.value.split(",")
    }

    fun getSpaceConfidentiality(space: String): AdministerConfidentiality {
        val spaceOptions = getSpaceConfidentialityOptions(space)
        val enabled = getSpaceConfidentiality(space, ConfigurationKeys.IS_CONFIDENTIALITY_ENABLED)?.toBoolean() ?: false
        return AdministerConfidentiality(enabled, spaceOptions)
    }

    fun getSpaceConfidentialityOptions(space: String): List<String> {
        //TODO: change getting default list from property file.
        return /*getSpaceConfidentiality(space, ConfigurationKeys.SPACE_CONFIDENTIALITY_LIST)?.split(",") ?:*/ DEFAULT_CONFIDENTIALITY_LIST
    }

    private fun getSpaceConfidentiality(spaceKey: String, propertyKey: String): String? {
        return propertyService.find().withSpaceKey(spaceKey).withPropertyKey(propertyKey).fetchOneOrNull()?.value?.value
    }

    private fun versionProvider(): Version {
        val user = AuthenticatedUserThreadLocal.get()
        val person = if (user == null) UnknownUser(null, "Anonymous")
            else KnownUser(null, user.name, user.fullName, user.key)
        return builder().by(person).`when`(Date()).message("confidentiality plugin").build()

    }

    private fun toSpaceProperty(spaceKey: String, property: String, propertyKey: String): JsonSpaceProperty {
        return JsonSpaceProperty.builder()
                .space(Reference.to(Space.builder().key(spaceKey).build()))
                .version(versionProvider())
                .key(propertyKey)
                .value(JsonString(property))
                .build()
    }

}
