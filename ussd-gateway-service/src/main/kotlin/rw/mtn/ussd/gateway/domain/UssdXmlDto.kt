package rw.mtn.ussd.gateway.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JsonIgnoreProperties(ignoreUnknown = true)
data class FreeflowDto(
    @field:JacksonXmlProperty(localName = "mode")
    var mode: String? = "FC"
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "request")
data class UssdXmlRequest(
    @field:JacksonXmlProperty(isAttribute = true, localName = "type")
    var type: String? = "pull",

    @field:JacksonXmlProperty(localName = "msisdn")
    var msisdn: String? = null,

    @field:JacksonXmlProperty(localName = "imsi")
    var imsi: String? = null,

    @field:JacksonXmlProperty(localName = "input")
    var input: String? = null,

    @field:JacksonXmlProperty(localName = "sessionid")
    var sessionid: String? = null,

    @field:JacksonXmlProperty(localName = "CellID")
    var cellId: String? = null,

    @field:JacksonXmlProperty(localName = "new_request")
    var newRequest: String? = null,

    @field:JacksonXmlProperty(localName = "parameters")
    var parameters: String? = null,

    @field:JacksonXmlProperty(localName = "freeflow")
    var freeflow: FreeflowDto? = null
)

@JacksonXmlRootElement(localName = "response")
data class UssdXmlResponse(
    @field:JacksonXmlProperty(localName = "msisdn")
    var msisdn: String? = null,

    @field:JacksonXmlProperty(localName = "sessionid")
    var sessionid: String? = null,

    @field:JacksonXmlProperty(localName = "freeflow")
    var freeflow: FreeflowDto? = FreeflowDto("FC"),

    @field:JacksonXmlProperty(localName = "message")
    var message: String? = null
)
