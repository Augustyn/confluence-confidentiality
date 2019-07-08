package ch.nine.confluence.confidentiality.repository

import ch.nine.confluence.confidentiality.repository.ConfigurationKeys.DEFAULT_CONFIDENTIALITY
import com.atlassian.confluence.api.model.Expansion
import com.atlassian.confluence.api.model.Expansions
import com.atlassian.confluence.api.model.JsonString
import com.atlassian.confluence.api.model.content.Content
import com.atlassian.confluence.api.model.content.ContentBody
import com.atlassian.confluence.api.model.content.ContentRepresentation.*
import com.atlassian.confluence.api.model.content.ContentType
import com.atlassian.confluence.api.model.content.JsonContentProperty
import com.atlassian.confluence.api.service.content.ContentPropertyService
import com.atlassian.confluence.core.ContentPropertyManager
import com.atlassian.confluence.pages.Page
import com.atlassian.confluence.spaces.Space
import org.apache.log4j.LogManager

/**
 * Repository responsible for storing and receiving confidentiality value, per page.
 */
class ConfidentialityRepository constructor(private val contentPropertyManager: ContentPropertyManager,
                                            private val contentService: ContentPropertyService) {
    companion object {
        private val log = LogManager.getLogger(this::class.java.name.substringBefore("\$Companion"))
    }

    fun getConfidentiality(page: Page?): String {
        return contentPropertyManager.getStringProperty(page, ConfigurationKeys.CONFIDENTIALITY) ?: DEFAULT_CONFIDENTIALITY
    }

    fun save(page: Page, newConfidentiality: String): String {
        contentPropertyManager.setStringProperty(page, ConfigurationKeys.CONFIDENTIALITY, newConfidentiality)
        return contentPropertyManager.getStringProperty(page, ConfigurationKeys.CONFIDENTIALITY) ?: DEFAULT_CONFIDENTIALITY
    }

    fun isConfidentialityEnabled(page: Page?): Boolean {
        val isEnabledString = contentPropertyManager.getStringProperty(page?.space?.description, ConfigurationKeys.IS_CONFIDENTIALITY_ENABLED)
        if (isEnabledString == null) contentPropertyManager.setStringProperty(page?.space?.description, ConfigurationKeys.IS_CONFIDENTIALITY_ENABLED, "false")
        return isEnabledString?.toBoolean() ?: false
    }

    fun getSpaceConfidentialityOptions(space: Space?): List<String> {
        return contentPropertyManager.getStringProperty(space?.description, ConfigurationKeys.SPACE_CONFIDENTIALITY_LIST)?.split(',') ?: ConfigurationKeys.DEFAULT_CONFIDENTIALITY_LIST
    }

    fun setSpaceConfidentialities(space: Space, confidentialityList: List<String>) {
        contentPropertyManager.setStringProperty(space.description, ConfigurationKeys.SPACE_CONFIDENTIALITY_LIST, confidentialityList.joinToString(separator = ","))
    }

    fun getConfidentialityFormService(space: Space) {
        val expansion = Expansion(DEFAULT_CONFIDENTIALITY, Expansions.of(space.key))
        val contentPropertyFinder = contentService.find(expansion)
        contentPropertyFinder.fetchPropertyKeys().forEach {
            log.info("For space with key: '${space.key}, found property: $it ")
        }
    }

    fun storeProperty(spaceKey: String, newConfidentiality: String): String {
        val jsonProp = JsonContentProperty.builder()
                .content(Content.builder(ContentType.valueOf(ConfigurationKeys.CONFIDENTIALITY))
                        .space(spaceKey)
                        .body(
                                ContentBody.contentBodyBuilder()
                                        .value(newConfidentiality)
                                        .representation(STORAGE)
                                        .build()
                        )
                        .build()
                ).value(JsonString(newConfidentiality))
                .key(ConfigurationKeys.CONFIDENTIALITY + spaceKey)
                .build()

        return contentService.create(jsonProp).value.value
    }


}