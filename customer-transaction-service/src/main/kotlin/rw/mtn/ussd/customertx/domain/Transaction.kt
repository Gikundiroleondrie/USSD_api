package rw.mtn.ussd.customertx.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

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

object SimulatedAccounts {
    private val balances = ConcurrentHashMap<String, Int>()
    const val DEFAULT_BALANCE = 1_000 // RWF

    fun balanceOf(phone: String): Int = balances.getOrDefault(phone, DEFAULT_BALANCE)

    fun debit(phone: String, amount: Int) {
        balances[phone] = balanceOf(phone) - amount
    }
}

@RestController
class CustomerTxServiceController(
    private val customerRepo: CustomerRepository,
    private val transactionRepo: TransactionRepository
) {

    @PostMapping("/api/customers/register-if-new")
    fun registerIfNew(@RequestBody req: CustomerRegisterRequestDto): ResponseEntity<Void> {
        val normalized = req.phoneNumber.removePrefix("+250").let {
            if (it.startsWith("7")) "0$it" else it
        }
        if (customerRepo.findByPhoneNumber(normalized) == null) {
            customerRepo.save(CustomerEntity(phoneNumber = normalized, active = true))
        }
        return ResponseEntity.ok().build()
    }

    @PostMapping("/api/transactions/process")
    fun processTransaction(@RequestBody req: TransactionProcessRequestDto): ResponseEntity<TransactionResultDto> {
        val balance = SimulatedAccounts.balanceOf(req.callerPhone)
        if (balance < req.price) {
            return ResponseEntity.ok(
                TransactionResultDto(
                    false,
                    "MTN Rwandacell Message\nMukiriya wacu, kohereza Me2U byanze kuko mufite inite zidahagije. Mushyiremo inite"
                )
            )
        }

        SimulatedAccounts.debit(req.callerPhone, req.price)

        val normalized = req.callerPhone.removePrefix("+250").let {
            if (it.startsWith("7")) "0$it" else it
        }
        val customer = customerRepo.findByPhoneNumber(normalized)
            ?: customerRepo.save(CustomerEntity(phoneNumber = normalized, active = true))

        transactionRepo.save(
            TransactionEntity(
                customer = customer,
                receiverNumber = req.recipientPhone,
                packageLabel = req.packageLabel,
                price = req.price
            )
        )

        return ResponseEntity.ok(TransactionResultDto(true, "Transaction completed successfully"))
    }
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
