package ch.nine.confluence.confidentiality.admin.model

import org.codehaus.jackson.annotate.JsonCreator
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

/**
 * Simple transport object, that holds information about confidentiality configuration
 * for JavaScript part of the plugin.
 */
@XmlRootElement
class AdministerConfidentialityOptions(private val options: List<String>) {

    @XmlElement
    fun getConfidentiality() : List<String> {
        return options
    }

    @JsonCreator
    fun create(option: String) : AdministerConfidentialityOptions {
        return AdministerConfidentialityOptions(listOf(option))
    }

    @JsonCreator
    fun create(options: List<String>) : AdministerConfidentialityOptions {
        return AdministerConfidentialityOptions(options)
    }
}