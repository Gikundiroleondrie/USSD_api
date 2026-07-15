package rw.mtn.ussd

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
class UssdController(
    private val ussdService: UssdService,
    @Value("\${ussd.api-key:}") private val loadedApiKey: String
) {

    @GetMapping("/debug/apikey")
    fun debugKey() = mapOf(
        "loaded" to loadedApiKey.ifBlank { "(blank — no key check active)" },
        "length" to loadedApiKey.length
    )

    @PostMapping("/ussd", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun handle(
        @RequestParam sessionId: String,
        @RequestParam serviceCode: String,
        @RequestParam phoneNumber: String,
        @RequestParam(required = false, defaultValue = "") text: String
    ): String {
        val response = ussdService.processInput(sessionId, phoneNumber, text)

        return response.removePrefix("CON ").removePrefix("END ")
    }
}