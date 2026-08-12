package rw.mtn.ussd.gateway.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rw.mtn.ussd.gateway.domain.UssdSession
import java.util.Optional

@Repository
interface SessionRepository : JpaRepository<UssdSession, String> {
    fun findBySessionIdAndPhone(sessionId: String, phone: String): Optional<UssdSession>
}
