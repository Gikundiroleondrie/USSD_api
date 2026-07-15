package rw.mtn.ussd.customer

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rw.mtn.ussd.transaction.TransactionEntity
import java.time.LocalDateTime

@Entity
@Table(name = "customers")
data class CustomerEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val phoneNumber: String = "",

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val transactions: List<TransactionEntity> = emptyList()
)

interface CustomerRepository : JpaRepository<CustomerEntity, Long> {
    fun findByPhoneNumber(phoneNumber: String): CustomerEntity?
    fun findByActive(active: Boolean): List<CustomerEntity>
}

data class CustomerRequest(
    val phoneNumber: String,
    val active: Boolean = true
)

data class CustomerDetailsResponse(
    val id: Long?,
    val phoneNumber: String,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val transactions: List<TransactionSummary>
)

data class TransactionSummary(
    val id: Long?,
    val receiverNumber: String,
    val packageLabel: String,
    val price: Int,
    val transactionDate: LocalDateTime
)

@RestController
@RequestMapping("/admin/customers")
class CustomerController(
    private val repo: CustomerRepository,
    private val transactionRepository: rw.mtn.ussd.transaction.TransactionRepository
    ) 
{

    @GetMapping
fun list(@RequestParam(required = false) active: Boolean?): List<CustomerDetailsResponse> {
    val customers = if (active != null) repo.findByActive(active) else repo.findAll()
    return customers.map { toResponse(it) }
}

@GetMapping("/{id}")
fun get(@PathVariable id: Long): ResponseEntity<CustomerDetailsResponse> =
    repo.findById(id).map { ResponseEntity.ok(toResponse(it)) }
        .orElse(ResponseEntity.notFound().build())

@GetMapping("/by-phone/{phone}")
fun getByPhone(@PathVariable phone: String): ResponseEntity<CustomerDetailsResponse> {
    val customer = repo.findByPhoneNumber(phone)
        ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(toResponse(customer))
}

private fun toResponse(c: CustomerEntity): CustomerDetailsResponse {
    val txs = transactionRepository.findByCustomer(c).map {
        TransactionSummary(
            id              = it.id,
            receiverNumber  = it.receiverNumber,
            packageLabel    = it.packageLabel,
            price           = it.price,
            transactionDate = it.transactionDate
        )
    }
    return CustomerDetailsResponse(
        id           = c.id,
        phoneNumber  = c.phoneNumber,
        active       = c.active,
        createdAt    = c.createdAt,
        transactions = txs
    )
}

    @PostMapping
    fun create(@RequestBody req: CustomerRequest): ResponseEntity<CustomerEntity> {
        val saved = repo.save(
            CustomerEntity(
                phoneNumber = req.phoneNumber,
                active      = req.active
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody req: CustomerRequest): ResponseEntity<CustomerEntity> {
        val existing = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val updated = existing.copy(
            phoneNumber = req.phoneNumber,
            active      = req.active
        )
        return ResponseEntity.ok(repo.save(updated))
    }

    @PatchMapping("/{id}/toggle")
    fun toggle(@PathVariable id: Long): ResponseEntity<CustomerEntity> {
        val existing = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(repo.save(existing.copy(active = !existing.active)))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build()
        repo.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}