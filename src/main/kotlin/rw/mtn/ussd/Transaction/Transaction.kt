package rw.mtn.ussd.transaction

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rw.mtn.ussd.customer.CustomerEntity
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class TransactionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    val customer: CustomerEntity? = null,

    @Column(nullable = false)
    val receiverNumber: String = "",

    @Column(nullable = false)
    val packageLabel: String = "",

    @Column(nullable = false)
    val price: Int = 0,

    @Column(nullable = false)
    val transactionDate: LocalDateTime = LocalDateTime.now()
)

interface TransactionRepository : JpaRepository<TransactionEntity, Long> {
    fun findByCustomer(customer: CustomerEntity): List<TransactionEntity>
    fun findByReceiverNumber(receiverNumber: String): List<TransactionEntity>
}

@RestController
@RequestMapping("/admin/transactions")
class TransactionController(private val repo: TransactionRepository) {

    @GetMapping
    fun list(): List<TransactionEntity> = repo.findAll()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<TransactionEntity> =
        repo.findById(id).map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/by-receiver/{phone}")
    fun byReceiver(@PathVariable phone: String) =
        repo.findByReceiverNumber(phone)
}