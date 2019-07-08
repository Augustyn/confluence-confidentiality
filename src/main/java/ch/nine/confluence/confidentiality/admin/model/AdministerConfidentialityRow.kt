package ch.nine.confluence.confidentiality.admin.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

/**
 * Simple transport object, that holds information about confidentiality configuration option
 * for JavaScript part of the plugin. Used for admin confidentiality rest table allowing
 * editing confidentiality options for space
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class AdministerConfidentialityRow(private val id: Int, private val confidentiality: String) {

    @XmlElement(name = "id")
    fun getId(): Int {
        return id
    }

    @XmlElement(name = "confidentiality")
    fun getConfidentiality() : String {
        return confidentiality
    }

}