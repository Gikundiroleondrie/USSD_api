package rw.mtn.ussd.gateway

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rw.mtn.ussd.gateway.domain.FreeflowDto
import rw.mtn.ussd.gateway.domain.UssdXmlRequest
import rw.mtn.ussd.gateway.domain.UssdXmlResponse

@RestController
class UssdController(
    private val ussdService: UssdService,
    private val sessionStore: SessionStore,
    @Value("\${ussd.api-key:}") private val loadedApiKey: String
) {
    private val logger = LoggerFactory.getLogger("io.ktor.server.Application")
    private val xmlMapper = XmlMapper()

    @GetMapping("/debug/apikey")
    fun debugKey() = mapOf(
        "loaded" to loadedApiKey.ifBlank { "(blank — no key check active)" },
        "length" to loadedApiKey.length
    )

    @RequestMapping(value = ["/api/v1/call_back", "/ussd"], method = [RequestMethod.GET, RequestMethod.POST])
    fun handle(
        @RequestParam(required = false) requestId: String?,
        @RequestParam(required = false) sessionId: String?,
        @RequestParam(required = false) serviceCode: String?,
        @RequestParam(required = false) phoneNumber: String?,
        @RequestParam(required = false) text: String?,
        @RequestBody(required = false) body: String?
    ): ResponseEntity<*> {
        val startTime = System.currentTimeMillis()

        if (!body.isNullOrBlank() && body.trim().startsWith("<")) {
            try {
                val xmlReq = xmlMapper.readValue(body, UssdXmlRequest::class.java)
                val rawPhone = xmlReq.msisdn ?: ""
                val normPhone = normalizePhone(rawPhone)
                val sid = xmlReq.sessionid?.takeIf { it.isNotBlank() }
                    ?: (System.currentTimeMillis() % 1_000_000_000L).toString()
                val reqId = xmlReq.newRequest?.trim() ?: "1"
                val inputVal = xmlReq.input?.trim() ?: ""

                val (resMessage, isEnd) = processRequest(reqId, sid, normPhone, inputVal)

                val duration = System.currentTimeMillis() - startTime
                logger.info("200 OK: POST (XML) - /ussd sid={} in {}ms", sid, duration)

                val xmlRes = UssdXmlResponse(
                    msisdn = null,
                    sessionid = null,
                    freeflow = FreeflowDto(if (isEnd) "FB" else "FC"),
                    message = resMessage
                )

                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xmlRes)
            } catch (e: Exception) {
                logger.error("Error parsing XML payload: {}", e.message)
            }
        }

        val finalReqId = requestId?.ifBlank { "1" } ?: "1"
        val finalPhone = normalizePhone(phoneNumber ?: "")
        val finalInput = text ?: ""

        val (resMessage, _) = processRequest(finalReqId, sessionId, finalPhone, finalInput)

        val duration = System.currentTimeMillis() - startTime
        logger.info("200 OK: GET/POST - /ussd in {}ms", duration)

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(resMessage)
    }

    private fun processRequest(
        requestId: String,
        sessionId: String?,
        phoneNumber: String,
        text: String
    ): Pair<String, Boolean> {
        return when (requestId) {
            "1" -> {
                val session = sessionStore.createSession(phoneNumber, sessionId)
                logger.info("USSD sid={} newRequest={} trail.size=1 position.size=1", session.sessionId, requestId)

                val response = ussdService.handle(phoneNumber, "", session.sessionId)
                val isEnd = response.startsWith("END")
                if (isEnd) sessionStore.clearSession(session.sessionId)
                val cleanMessage = response.removePrefix("CON ").removePrefix("END ")
                Pair(cleanMessage, isEnd)
            }

            "0" -> {
                if (sessionId.isNullOrBlank()) {
                    return Pair("MTN Rwanda Message\nSession invalid or has expired. Please dial again.", true)
                }
                if (text.isBlank()) {
                    val response = ussdService.invalidInput(phoneNumber)
                    val isEnd = response.startsWith("END")
                    if (isEnd) sessionStore.clearSession(sessionId)
                    return Pair(response.removePrefix("CON ").removePrefix("END "), isEnd)
                }

                val accumulated = sessionStore.appendText(sessionId, text, phoneNumber)
                    ?: return Pair("MTN Rwanda Message\nSession invalid or has expired. Please dial again.", true)

                val activeSession = sessionStore.getSession(sessionId, phoneNumber)
                val steps = accumulated.split("*").filter { it.isNotBlank() }
                val trailSize = steps.size
                val positionSize = activeSession?.level ?: steps.size

                logger.info("USSD sid={} newRequest={} trail.size={} position.size={}", sessionId, requestId, trailSize, positionSize)

                val response = ussdService.handle(phoneNumber, accumulated, sessionId)
                val isEnd = response.startsWith("END")
                if (isEnd) sessionStore.clearSession(sessionId)
                val cleanMessage = response.removePrefix("CON ").removePrefix("END ")
                Pair(cleanMessage, isEnd)
            }

            else -> Pair("requestId must be 1 or 0", true)
        }
    }

    private val mtnPhoneRegex = Regex("^07[89]\\d{7}$")

    private fun normalizePhone(raw: String): String {
        if (raw.isBlank()) return ""
        var cleaned = raw.trim().removePrefix("+")
        if (cleaned.startsWith("250")) {
            cleaned = "0" + cleaned.substring(3)
        }
        return cleaned
    }

    private fun isValidMtnPhone(phone: String): Boolean {
        if (phone.isBlank()) return true
        val normalized = normalizePhone(phone)
        return mtnPhoneRegex.matches(normalized)
    }
}
