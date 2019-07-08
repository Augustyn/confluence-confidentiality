package ch.nine.confluence.confidentiality.repository;

/**
 * Companion object, storing constants keys that are used to store plugin confidentiality options.
 */
object ConfigurationKeys {
    const val CONFIDENTIALITY = "ch.nine.confluence-confidentiality.value"
    const val IS_CONFIDENTIALITY_ENABLED = "ch.nine.confluence-confidentiality.enabled"
    const val SPACE_CONFIDENTIALITY_LIST = "ch.nine.confluence-confidentiality.space.confidentiality"
    const val DEFAULT_CONFIDENTIALITY = "confidential"
    val DEFAULT_CONFIDENTIALITY_LIST = listOf("public", "internal", "confidential")
}
