package rw.mtn.ussd.gateway

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import rw.mtn.ussd.gateway.domain.UssdSession
import rw.mtn.ussd.gateway.repository.SessionRepository
import java.time.Duration
import java.time.Instant

data class SessionData(
    val sessionId: String,
    val phoneNumber: String,
    var accumulatedText: String = "",
    var transactionId: String? = null,
    var path: String = "",
    var level: Int = 1,
    val createdAt: Instant = Instant.now(),
    var lastAccessedAt: Instant = Instant.now()
)

@Component
class SessionStore(
    private val sessionRepository: SessionRepository
) {
    private val logger = LoggerFactory.getLogger(SessionStore::class.java)
    private val timeoutSeconds = 60L

    fun createSession(phoneNumber: String, requestedSessionId: String? = null): SessionData {
        val sessionId = requestedSessionId?.takeIf { it.isNotBlank() }
            ?: (System.currentTimeMillis() % 1_000_000_000L).toString()
        val now = Instant.now()

        val existingOpt = sessionRepository.findById(sessionId)
        if (existingOpt.isPresent) {
            val entity = existingOpt.get()
            if (phoneNumber.isNotBlank()) {
                entity.phone = phoneNumber
            }
            entity.path = ""
            entity.level = 1
            entity.lastAccessedAt = now
            sessionRepository.save(entity)
            logger.info("Session reset for request: sid={} phone={}", sessionId, entity.phone)
            return SessionData(
                sessionId = entity.sessionId,
                phoneNumber = entity.phone,
                accumulatedText = "",
                transactionId = entity.transactionId,
                path = "",
                level = 1,
                createdAt = entity.createdAt,
                lastAccessedAt = entity.lastAccessedAt
            )
        }

        val sessionData = SessionData(
            sessionId = sessionId,
            phoneNumber = phoneNumber,
            path = "",
            level = 1,
            createdAt = now,
            lastAccessedAt = now
        )

        val entity = UssdSession(
            sessionId = sessionId,
            phone = phoneNumber,
            transactionId = null,
            path = "",
            level = 1,
            createdAt = now,
            lastAccessedAt = now
        )
        sessionRepository.save(entity)
        logger.info("Session created & saved to DB: sid={} phone={}", sessionId, phoneNumber)
        return sessionData
    }

    fun getSession(sessionId: String, phoneNumber: String? = null): SessionData? {
        val entityOpt = sessionRepository.findById(sessionId)
        if (!entityOpt.isPresent) return null
        val entity = entityOpt.get()

        if (phoneNumber != null && phoneNumber.isNotBlank() && entity.phone != phoneNumber) {
            logger.warn("Session phone mismatch: sid={} expected={} actual={}", sessionId, phoneNumber, entity.phone)
            return null
        }

        val secondsInactive = Duration.between(entity.lastAccessedAt, Instant.now()).seconds
        if (secondsInactive > timeoutSeconds) {
            logger.info("Session expired after {} seconds of inactivity: sid={}", secondsInactive, sessionId)
            sessionRepository.delete(entity)
            return null
        }

        entity.lastAccessedAt = Instant.now()
        sessionRepository.save(entity)

        return SessionData(
            sessionId = entity.sessionId,
            phoneNumber = entity.phone,
            accumulatedText = if (entity.path.isBlank()) "" else entity.path.replace(",", "*"),
            transactionId = entity.transactionId,
            path = entity.path,
            level = entity.level,
            createdAt = entity.createdAt,
            lastAccessedAt = entity.lastAccessedAt
        )
    }

    fun appendText(sessionId: String, newInput: String, phoneNumber: String): String? {
        val session = getSession(sessionId, phoneNumber) ?: run {
            logger.warn("Cannot append text: session invalid or expired for sid={}", sessionId)
            return null
        }

        val currentSteps = if (session.path.isBlank()) mutableListOf() else session.path.split(",").toMutableList()
        if (newInput == "0") {
            if (currentSteps.isNotEmpty()) currentSteps.removeLast()
        } else {
            currentSteps.add(newInput)
        }

        val newPath = currentSteps.joinToString(",")
        val accumulatedText = currentSteps.joinToString("*")
        val level = if (currentSteps.isEmpty()) 1 else currentSteps.size

        session.path = newPath
        session.accumulatedText = accumulatedText
        session.level = level

        sessionRepository.findById(sessionId).ifPresent { entity ->
            if (phoneNumber.isNotBlank()) entity.phone = phoneNumber
            entity.path = newPath
            entity.level = level
            entity.lastAccessedAt = Instant.now()
            sessionRepository.save(entity)
        }

        return accumulatedText
    }

    fun setTransactionId(sessionId: String, transactionId: String) {
        sessionRepository.findById(sessionId).ifPresent { entity ->
            entity.transactionId = transactionId
            sessionRepository.save(entity)
            logger.info("Transaction ID saved to session sid={}: txId={}", sessionId, transactionId)
        }
    }

    fun clearSession(sessionId: String) {
        sessionRepository.findById(sessionId).ifPresent { entity ->
            entity.lastAccessedAt = Instant.now()
            sessionRepository.save(entity)
            logger.info("Session flow finished, saved to DB: sid={}", sessionId)
        }
    }

    fun rollbackLastStep(sessionId: String) {
        val session = getSession(sessionId) ?: return
        val steps = if (session.path.isBlank()) mutableListOf() else session.path.split(",").toMutableList()
        if (steps.isNotEmpty()) steps.removeLast()

        val newPath = steps.joinToString(",")
        val level = if (steps.isEmpty()) 1 else steps.size

        sessionRepository.findById(sessionId).ifPresent { entity ->
            entity.path = newPath
            entity.level = level
            entity.lastAccessedAt = Instant.now()
            sessionRepository.save(entity)
        }
    }

    fun resetText(sessionId: String) {
        sessionRepository.findById(sessionId).ifPresent { entity ->
            entity.path = ""
            entity.level = 1
            entity.lastAccessedAt = Instant.now()
            sessionRepository.save(entity)
        }
    }

    fun cleanupExpired() {
        val cutoff = Instant.now().minusSeconds(timeoutSeconds)
        val expired = sessionRepository.findAll().filter { it.lastAccessedAt.isBefore(cutoff) }
        if (expired.isNotEmpty()) {
            sessionRepository.deleteAll(expired)
            logger.info("Cleaned up {} expired USSD sessions", expired.size)
        }
    }
}
