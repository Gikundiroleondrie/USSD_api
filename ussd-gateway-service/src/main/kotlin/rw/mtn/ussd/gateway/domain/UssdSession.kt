package rw.mtn.ussd.gateway.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "sessions")
class UssdSession(
    @Id
    @Column(name = "session_id", nullable = false)
    var sessionId: String = "",

    @Column(name = "phone", nullable = false)
    var phone: String = "",

    @Column(name = "transaction_id", nullable = true)
    var transactionId: String? = null,

    @Column(name = "path", length = 1000)
    var path: String = "",

    @Column(name = "level")
    var level: Int = 1,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "last_accessed_at")
    var lastAccessedAt: Instant = Instant.now()
)
