package rw.mtn.ussd

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.beans.factory.annotation.Value

@RestController
class UssdController(
    private val ussdService: UssdService,
    private val sessionStore: SessionStore,
    @Value("\${ussd.api-key:}") private val loadedApiKey: String
) {

    @GetMapping("/debug/apikey")
    fun debugKey() = mapOf(
        "loaded" to loadedApiKey.ifBlank { "(blank — no key check active)" },
        "length" to loadedApiKey.length
    )

    @PostMapping("/ussd", produces = [MediaType.TEXT_PLAIN_VALUE])
fun handle(
    @RequestParam requestId: String,
    @RequestParam(required = false) sessionId: String?,
    @RequestParam serviceCode: String,
    @RequestParam phoneNumber: String,
    @RequestParam(required = false, defaultValue = "") text: String
): ResponseEntity<String> {

    return when (requestId) {

        "1" -> {
            val session = sessionStore.createSession(phoneNumber, sessionId)
            val response = ussdService.handle(phoneNumber, "")
            if (response.startsWith("END")) sessionStore.clearSession(session.sessionId)
            ResponseEntity.ok(
                response.removePrefix("CON ").removePrefix("END ")
            )
        }

        "0" -> {
            if (sessionId.isNullOrBlank()) {
                return ResponseEntity.badRequest().body("sessionId is required when requestId is 0")
            }
            val accumulated = sessionStore.appendText(sessionId, text, phoneNumber)
                ?: return ResponseEntity.status(410).body(
                    "Session not found or has expired. Please dial again."
                )
            val response = ussdService.handle(phoneNumber, accumulated)
            if (response.startsWith("END")) sessionStore.clearSession(sessionId)
            ResponseEntity.ok(
                response.removePrefix("CON ").removePrefix("END ")
            )
        }

        else -> ResponseEntity.badRequest().body("requestId must be 1 or 0")
    }
}

}