package ch.nine.confluence.confidentiality.api.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
class NotFound(private val message: String) {

    @XmlElement
    fun getMessage() : String {
        return message
    }

    @XmlElement
    fun getErrorCode() : Int {
        return 404
    }
}
