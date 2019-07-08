package ch.nine.confluence.confidentiality.admin.model

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

/**
 * Simple transport object, that holds information about confidentiality configuration
 * for JavaScript part of the plugin.
 */
@XmlRootElement
class AdministerConfidentiality(private val enabled: Boolean,
                                private val options: List<String>) {

    @XmlElement
    fun getEnabled() : Boolean {
        return enabled
    }

    @XmlElement
    fun getConfidentiality() : List<String> {
        return options
    }

}