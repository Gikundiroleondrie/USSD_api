package rw.mtn.ussd.gateway.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

data class CustomerRegisterRequest(val phoneNumber: String)

data class TransactionProcessRequest(
    val callerPhone: String,
    val recipientPhone: String,
    val packageLabel: String,
    val price: Int
)

data class TransactionResultDto(
    val success: Boolean,
    val message: String
)

@Component
class CustomerTransactionClient(
    @Value("\${services.customer-transaction.url:http://localhost:8082}") private val serviceUrl: String,
    private val restTemplate: RestTemplate = RestTemplate()
) {

    fun registerCustomerIfNew(phoneNumber: String) {
        try {
            val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            val entity = HttpEntity(CustomerRegisterRequest(phoneNumber), headers)
            restTemplate.postForEntity("$serviceUrl/api/customers/register-if-new", entity, Void::class.java)
        } catch (e: Exception) {
            println("CustomerTransactionClient registerIfNew error: ${e.javaClass.name} - ${e.message}")
        }
    }

    fun processTransaction(callerPhone: String, recipientPhone: String, packageLabel: String, price: Int): TransactionResultDto {
        return try {
            val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            val request = TransactionProcessRequest(callerPhone, recipientPhone, packageLabel, price)
            val entity = HttpEntity(request, headers)
            val response = restTemplate.postForEntity("$serviceUrl/api/transactions/process", entity, TransactionResultDto::class.java)
            response.body ?: TransactionResultDto(false, "Service response error")
        } catch (e: Exception) {
            println("CustomerTransactionClient error: ${e.javaClass.name} - ${e.message}")
            e.printStackTrace()
            TransactionResultDto(false, "Transaction service unavailable")
        }
    }
}
