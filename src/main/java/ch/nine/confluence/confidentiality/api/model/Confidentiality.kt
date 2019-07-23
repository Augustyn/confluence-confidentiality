package ch.nine.confluence.confidentiality.api.model

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

/**
 * Simple transport object, that holds information about confidentiality
 * for JavaScript part of the plugin.
 */
@XmlRootElement
class Confidentiality(private val enabled: Boolean,
                      private val confidentiality: String?,
                      private val possibleConfidentialities: List<String>?,
                      private val canUserEdit: Boolean?) {
    @XmlElement
    fun isEnabled() : Boolean {
        return enabled
    }

    @XmlElement
    fun getConfidentiality() : String? {
        return confidentiality
    }

    @XmlElement
    fun getPossibleConfidentialities() : List<String>? {
        return possibleConfidentialities
    }

    @XmlElement
    fun getCanUserEdit() : Boolean? {
        return canUserEdit
    }

}