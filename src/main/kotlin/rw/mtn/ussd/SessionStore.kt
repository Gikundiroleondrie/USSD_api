package rw.mtn.ussd

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

data class SessionData(
    val sessionId: String,
    val phoneNumber: String,
    var accumulatedText: String = "",
    val createdAt: Instant = Instant.now(),
    var lastAccessedAt: Instant = Instant.now()
)

@Component
class SessionStore {

    private val store = ConcurrentHashMap<String, SessionData>()

    fun createSession(phoneNumber: String): SessionData {
        val sessionId = java.util.UUID.randomUUID().toString()
        val session = SessionData(
            sessionId = sessionId,
            phoneNumber = phoneNumber
        )
        store[sessionId] = session
        return session
    }

    fun getSession(sessionId: String): SessionData? {
        val session = store[sessionId] ?: return null
        val inactiveSeconds = java.time.Duration.between(session.lastAccessedAt, Instant.now()).seconds
        if (inactiveSeconds > 120) {
            store.remove(sessionId)
            return null
        }
        session.lastAccessedAt = Instant.now()
        return session
    }

    fun appendText(sessionId: String, newInput: String): String? {
    val session = getSession(sessionId) ?: return null
    
    if (newInput == "0") {
        val steps = session.accumulatedText.split("*").filter { it.isNotBlank() }
        session.accumulatedText = if (steps.size <= 1) "" else steps.dropLast(1).joinToString("*")
    } else {
        session.accumulatedText = if (session.accumulatedText.isBlank()) newInput
                                  else "${session.accumulatedText}*$newInput"
    }
    return session.accumulatedText
}

    fun clearSession(sessionId: String) {
        store.remove(sessionId)
    }

    fun cleanupExpired() {
        val now = Instant.now()
        store.entries.removeIf {
            java.time.Duration.between(it.value.lastAccessedAt, now).seconds > 120
        }
    }
}