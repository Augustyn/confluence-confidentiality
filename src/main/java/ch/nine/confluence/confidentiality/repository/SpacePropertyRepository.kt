package ch.nine.confluence.confidentiality.repository

import ch.nine.confluence.confidentiality.admin.model.AdministerConfidentiality
import ch.nine.confluence.confidentiality.admin.model.AdministerConfidentialityRow
import ch.nine.confluence.confidentiality.repository.ConfigurationKeys.CONFIDENTIALITY_ENABLED
import ch.nine.confluence.confidentiality.repository.ConfigurationKeys.CONFIDENTIALITY_LIST
import com.atlassian.confluence.api.model.Expansion
import com.atlassian.confluence.api.model.JsonString
import com.atlassian.confluence.api.model.content.JsonSpaceProperty
import com.atlassian.confluence.api.model.content.JsonSpaceProperty.Expansions.VERSION
import com.atlassian.confluence.api.model.content.Space
import com.atlassian.confluence.api.model.content.Version
import com.atlassian.confluence.api.model.content.Version.builder
import com.atlassian.confluence.api.model.people.KnownUser
import com.atlassian.confluence.api.model.people.UnknownUser
import com.atlassian.confluence.api.model.reference.Reference
import com.atlassian.confluence.api.service.content.ContentPropertyService.MAXIMUM_VALUE_LENGTH
import com.atlassian.confluence.api.service.content.SpacePropertyService
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import com.atlassian.confluence.util.i18n.I18NBeanFactory
import com.atlassian.fugue.Option
import com.atlassian.fugue.Option.option
import org.apache.log4j.LogManager
import java.util.*

class SpacePropertyRepository constructor(private val propertyService: SpacePropertyService,
                                          private val i18nFactory: I18NBeanFactory) {
    companion object {
        private val log = LogManager.getLogger(this::class.java.name.substringBefore("\$Companion"))
        private const val DEFAULT_CONFIDENTIALITY = false
        private val expansion = Expansion(VERSION)
    }

    fun isConfidentialityEnabled(spaceKey: String): Boolean {
        return getConfidentialityEnabled(spaceKey)?.toBoolean() ?: DEFAULT_CONFIDENTIALITY
    }

    fun storeProperty(spaceKey: String, isEnabled: Boolean): Boolean {
        val storedValue = storePropertyEnabled(spaceKey, isEnabled.toString())
                .get()?.value?.value?.toBoolean() ?: isEnabled
        return !storedValue
    }

    fun storeProperty(spaceKey: String, list: List<AdministerConfidentialityRow>): List<AdministerConfidentialityRow> {
        val stringOptionList = list.map { "${it.getId()}#${it.getConfidentiality()}" }.joinToString { it }
        if (stringOptionList.length > MAXIMUM_VALUE_LENGTH) {
            log.warn("Cannot save confidentiality list: '$stringOptionList. Too long. Max value: $MAXIMUM_VALUE_LENGTH")
            //TODO: maybe it would be better to throw Exception, cut of some values, or validate maximum length in controller or service?
        }
        return storePropertyList(spaceKey, stringOptionList)
                .get().value.value
                .split(",")
                .map {
                    val row = it.split("#")
                    AdministerConfidentialityRow(numOrDefault(row[0], list.size), row[1].trim())
                }
    }

    fun getSpaceConfidentialityProperty(space: String): AdministerConfidentiality {
        val spaceOptions = getSpaceConfidentialityOptions(space)
        val enabled = getSpaceConfidentialityProperty(space, CONFIDENTIALITY_ENABLED)?.toBoolean() ?: false
        return AdministerConfidentiality(enabled, spaceOptions.map { it.getConfidentiality() })
    }

    fun getSpaceConfidentialityOptions(space: String): List<AdministerConfidentialityRow> {
        return getConfidentialityList(space)
                ?.split(",")
                ?.mapIndexed { i, s ->
                    val row = s.split("#")
                    val idx = numOrDefault(row[0], i + 1)
                    AdministerConfidentialityRow(idx, row[1].trim())
                } ?: getDefaultOptionsList()
    }

    private fun versionProvider(version: Int?): Version {
        val user = AuthenticatedUserThreadLocal.get()
        val person = if (user == null) UnknownUser(null, "Anonymous")
        else KnownUser(null, user.name, user.fullName, user.key)
        return builder()
                .by(person)
                .`when`(Date())
                .message("confidentiality plugin")
                .number(version ?: 1)
                .build()
    }

    private fun getDefaultOptionsList(): List<AdministerConfidentialityRow> {
        val i18NBean = i18nFactory.i18NBean
        return i18NBean.getText("confluence-confidentiality.default.confidentiality.list")
                .split(",")
                .mapIndexed { i, o -> AdministerConfidentialityRow(i + 1, o.toLowerCase().trim()) }
    }

    private fun numOrDefault(num: String, idx: Int): Int {
        return try {
            Integer.valueOf(num.trim())
        } catch (e: NumberFormatException) {
            log.info("Failed to convert string: $num, to integer. Returning fallback: $idx")
            idx
        }
    }

    private fun toSpaceProperty(spaceKey: String, property: String, version: Int, propertyKey: String): JsonSpaceProperty {
        return JsonSpaceProperty.builder()
                .space(Reference.to(Space.builder().key(spaceKey).build()))
                .version(versionProvider(version))
                .key(propertyKey)
                .value(JsonString(property))
                .build()
    }

    private fun storeSpaceProperty(spaceKey: String, propertyString: String, propertyKey: String): Option<JsonSpaceProperty> {
        return propertyService.find(expansion)
                .withSpaceKey(spaceKey)
                .withPropertyKey(propertyKey)
                .fetchOne()
                .onEach {
                    val oldVersion = it.version?.number
                    propertyService.update(toSpaceProperty(spaceKey, propertyString, (oldVersion?.plus(1) ?: 1), propertyKey))
                }.orElse {
                    option(propertyService.create(toSpaceProperty(spaceKey, propertyString, 1, propertyKey)))
                }
    }

    private fun storePropertyEnabled(spaceKey: String, property: String) = storeSpaceProperty(spaceKey, property, CONFIDENTIALITY_ENABLED)
    private fun getConfidentialityEnabled(spaceKey: String) = getSpaceConfidentialityProperty(spaceKey, CONFIDENTIALITY_ENABLED)

    private fun storePropertyList(spaceKey: String, property: String) = storeSpaceProperty(spaceKey, property, CONFIDENTIALITY_LIST)
    private fun getConfidentialityList(spaceKey: String) = getSpaceConfidentialityProperty(spaceKey, CONFIDENTIALITY_LIST)

    private fun getSpaceConfidentialityProperty(spaceKey: String, propertyKey: String): String? {
        return propertyService.find(expansion).withSpaceKey(spaceKey).withPropertyKey(propertyKey).fetchOneOrNull()?.value?.value
    }

    private fun removeSpaceProperty(spaceKey: String, property: String?) {
        propertyService.find(expansion)
                .withSpaceKey(spaceKey)
                .withPropertyKey(property ?: CONFIDENTIALITY_LIST)
                .fetchOne()
                .onEach {
                    propertyService.delete(toSpaceProperty(spaceKey, it.value.value, (it.version?.number ?: 1), property
                            ?: CONFIDENTIALITY_LIST))
                }
    }

}
