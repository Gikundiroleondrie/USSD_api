package rw.mtn.ussd

data class Pkg(
    val label: String,
    val price: Int,
    val confirmText: String
)

object SimulatedAccounts {

    private val balances = mutableMapOf<String, Int>()
    const val DEFAULT_BALANCE = 1_000 // RWF

    fun balanceOf(phone: String): Int = balances.getOrDefault(phone, DEFAULT_BALANCE)

    fun debit(phone: String, amount: Int) {
        balances[phone] = balanceOf(phone) - amount
    }
}
