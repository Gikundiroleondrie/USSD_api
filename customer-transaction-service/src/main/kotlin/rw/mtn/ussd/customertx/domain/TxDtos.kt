package rw.mtn.ussd.customertx.domain

data class CustomerRegisterRequestDto(
    val phoneNumber: String = ""
)

data class TransactionProcessRequestDto(
    val callerPhone: String = "",
    val recipientPhone: String = "",
    val packageLabel: String = "",
    val price: Int = 0
)

data class TransactionResultDto(
    val success: Boolean = false,
    val message: String = ""
)
