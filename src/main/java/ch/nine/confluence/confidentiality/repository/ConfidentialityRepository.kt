package ch.nine.confluence.confidentiality.repository

import ch.nine.confluence.confidentiality.repository.ConfigurationKeys.CONFIDENTIALITY_SELECTED
import ch.nine.confluence.confidentiality.repository.ConfigurationKeys.DEFAULT_CONFIDENTIALITY
import com.atlassian.confluence.core.ContentPropertyManager
import com.atlassian.confluence.pages.Page
import com.atlassian.confluence.util.i18n.I18NBeanFactory

/**
 * Repository responsible for storing and receiving confidentiality value, per page.
 */
class ConfidentialityRepository constructor(private val contentPropertyManager: ContentPropertyManager,
                                            private val i18nFactory: I18NBeanFactory) {

    fun getConfidentiality(page: Page?): String {
        return contentPropertyManager.getStringProperty(page, CONFIDENTIALITY_SELECTED) ?: i18nFactory.i18NBean.getText(DEFAULT_CONFIDENTIALITY)
    }

    fun save(page: Page, newConfidentiality: String): String {
        contentPropertyManager.setStringProperty(page, CONFIDENTIALITY_SELECTED, newConfidentiality)
        return contentPropertyManager.getStringProperty(page, CONFIDENTIALITY_SELECTED) ?: i18nFactory.i18NBean.getText(DEFAULT_CONFIDENTIALITY)
    }

}