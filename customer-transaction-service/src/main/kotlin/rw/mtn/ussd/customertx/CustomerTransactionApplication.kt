package rw.mtn.ussd.customertx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CustomerTransactionApplication

fun main(args: Array<String>) {
    runApplication<CustomerTransactionApplication>(*args)
}
