package rw.mtn.ussd

data class UssdRequest(
    val requestId: String,       
    val sessionId: String?,      
    val serviceCode: String,
    val phoneNumber: String,
    val text: String = ""
)

data class UssdResponse(
    val sessionId: String,
    val response: String
)