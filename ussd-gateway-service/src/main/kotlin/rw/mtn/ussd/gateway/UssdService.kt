package rw.mtn.ussd.gateway

import org.springframework.stereotype.Service
import rw.mtn.ussd.gateway.client.CustomerTransactionClient
import rw.mtn.ussd.gateway.client.MenuDto
import rw.mtn.ussd.gateway.client.MenuPackageClient
import rw.mtn.ussd.gateway.client.PkgDto

@Service
class UssdService(
    private val menuPackageClient: MenuPackageClient,
    private val customerTransactionClient: CustomerTransactionClient,
    private val sessionStore: SessionStore
) {

    private val mtnPhoneRegex = Regex("^07[89]\\d{7}$")
    private val languagePrefs = mutableMapOf<String, String>()
    private val sessionPaths = mutableMapOf<String, MutableList<String>>()

    private fun isEn(phone: String) = languagePrefs[phone] == "en"

    fun processInput(sessionId: String, phoneNumber: String, rawText: String): String {
        customerTransactionClient.registerCustomerIfNew(phoneNumber)

        val path = sessionPaths.getOrPut(sessionId) { mutableListOf() }
        val newInput = if (rawText.isBlank()) "" else rawText.split("*").last()

        when {
            newInput.isBlank() -> path.clear()
            newInput == "0" -> {
                if (path.isNotEmpty()) path.removeLast()
            }
            else -> path.add(newInput)
        }

        val replayText = path.joinToString("*")
        val response = handle(phoneNumber, replayText, sessionId)
        if (response.startsWith("END")) sessionPaths.remove(sessionId)
        return response
    }

    fun clearSession(sessionId: String) = sessionPaths.remove(sessionId)

    fun handle(phoneNumber: String, text: String, sessionId: String? = null): String {
        customerTransactionClient.registerCustomerIfNew(phoneNumber)

        val steps = if (text.isBlank()) emptyList() else text.split("*")
        if (steps.isEmpty()) return renderMenu("main", phoneNumber)

        val mainItems = menuPackageClient.getMenuItems("main")
        if (mainItems.isEmpty()) return END("MTN Rwandacell Message\nService unavailable")

        val mainChoice = steps[0].toIntOrNull()
        if (mainChoice == null || mainChoice < 1 || mainChoice > mainItems.size) {
            return invalidInput(phoneNumber)
        }

        val mainItem = mainItems[mainChoice - 1]

        if (mainItem.targetKey == "changelanguage") {
            languagePrefs[phoneNumber] = if (isEn(phoneNumber)) "rw" else "en"
            if (sessionId != null) {
                sessionStore.resetText(sessionId)
                sessionPaths.remove(sessionId)
            }
            return renderMenu("main", phoneNumber)
        }

        if (mainItem.requiresPhone) {
            if (steps.size == 1) return phonePrompt(phoneNumber)
            val recipient = validatedPhone(steps[1]) ?: run {
                if (sessionId != null) {
                    sessionStore.rollbackLastStep(sessionId)
                }
                return invalidPhone(phoneNumber)
            }
            return resolveTarget(mainItem.targetKey, steps, 2, recipient, phoneNumber, sessionId)
        } else {
            return resolveTarget(mainItem.targetKey, steps, 1, phoneNumber, phoneNumber, sessionId)
        }
    }

    private fun resolveTarget(
        targetKey: String,
        steps: List<String>,
        fromIndex: Int,
        recipient: String,
        callerPhone: String,
        sessionId: String? = null
    ): String {
        return when (targetKey) {
            "routerbundles" -> END(
                if (isEn(callerPhone)) "MTN Rwanda Message\nComing soon"
                else "MTN Rwandacell Message\nComing soon"
            )
            else -> {
                val subItems = menuPackageClient.getMenuItems(targetKey)
                if (subItems.isNotEmpty()) {
                    handleSubMenu(targetKey, subItems, steps, fromIndex, recipient, callerPhone, sessionId)
                } else {
                    val packages = menuPackageClient.getPackages(targetKey)
                    simplePackageFlow(
                        steps, fromIndex, recipient, callerPhone,
                        packages,
                        sessionId = sessionId,
                        onBack = { renderMenu(parentOf(targetKey), callerPhone) }
                    )
                }
            }
        }
    }

    private fun handleSubMenu(
        menuKey: String,
        items: List<MenuDto>,
        steps: List<String>,
        fromIndex: Int,
        recipient: String,
        callerPhone: String,
        sessionId: String? = null
    ): String {
        if (steps.size == fromIndex) return renderMenu(menuKey, callerPhone, items)

        val choice = steps[fromIndex]
        if (choice == "0") return renderMenu(parentOf(menuKey), callerPhone)

        val idx = choice.toIntOrNull()
        if (idx == null || idx < 1 || idx > items.size) return invalidInput(callerPhone)

        val chosen = items[idx - 1]
        return resolveTarget(chosen.targetKey, steps, fromIndex + 1, recipient, callerPhone, sessionId)
    }

    private fun renderMenu(menuKey: String, phone: String, items: List<MenuDto>? = null): String {
        val list = items ?: menuPackageClient.getMenuItems(menuKey)
        if (list.isEmpty()) return END("MTN Rwandacell Message\nService unavailable")

        val lines = list.mapIndexed { i, item ->
            "${i + 1})${if (isEn(phone)) item.labelEn else item.labelRw}"
        }
        val back = if (menuKey != "main") listOf(backOption(phone)) else emptyList()
        return CON((lines + back).joinToString("\n"))
    }

    private fun parentOf(targetKey: String): String = when (targetKey) {
        "voicepack"       -> "main"
        "bundleinternet"  -> "main"
        "prestige"        -> "prestigemenu"
        "prestigemenu"    -> "main"
        "amahanga"        -> "voicepack"
        "amahangaUmunsi",
        "amahangaIcyumweru",
        "amahangaUkwezi" -> "amahanga"
        "internetirekure" -> "bundleinternet"
        "internetIrekureUmunsi",
        "internetIrekureIcyumweru",
        "internetIrekureUkwezi" -> "internetirekure"
        "socialmedia"    -> "bundleinternet"
        "socialWhatsapp",
        "socialFacebook" -> "socialmedia"
        "gumamo", "irekure24hrs", "irekureIcyumweru",
        "irekureUkwezi", "voiceGwamon", "desade",
        "voiceFoLeva"    -> "voicepack"
        "tubitayeho", "dataGwamon", "dataFoLeva" -> "bundleinternet"
        else             -> "main"
    }

    fun invalidInput(phone: String) =
        END(if (isEn(phone)) "MTN Rwanda Message\nInvalid input" else "MTN Rwandacell Message\nInvalid input")

    fun invalidPhone(phone: String) =
        CON(if (isEn(phone)) "MTN Rwanda Message\nInvalid MTN recipient number (Must start with 078 or 079)" else "MTN Rwandacell Message\nUshyizemo numero itari iy'MTN. Shyiramo numero itangira na 078 cyangwa 079")

    private fun phonePrompt(phone: String) =
        CON(if (isEn(phone)) "Enter MTN recipient number (078xxxxxxx / 079xxxxxxx)" else "Shyiramo numero ya MTN (078xxxxxxx / 079xxxxxxx)")

    private fun validatedPhone(raw: String): String? {
        val normalized = normalizePhone(raw)
        return if (mtnPhoneRegex.matches(normalized)) normalized else null
    }

    private fun normalizePhone(raw: String): String {
        if (raw.isBlank()) return ""
        var cleaned = raw.trim().removePrefix("+")
        if (cleaned.startsWith("250")) {
            cleaned = "0" + cleaned.substring(3)
        }
        return cleaned
    }

    private fun backOption(phone: String) =
        if (isEn(phone)) "0)Back" else "0)Gusubira Inyuma"

    private fun packageScreen(list: List<PkgDto>, phone: String): String {
        val lines = list.mapIndexed { i, pkg -> "${i + 1})${pkg.label}" }
        return CON((lines + backOption(phone)).joinToString("\n"))
    }

    private fun pick(list: List<PkgDto>, choice: String): PkgDto? =
        choice.toIntOrNull()?.let { list.getOrNull(it - 1) }

    private fun confirmScreen(pkg: PkgDto, recipient: String, phone: String): String = CON(
        if (isEn(phone)) """
            Hello, you are sending ${pkg.confirmText} to $recipient
            1)Confirm
            2)Cancel
            0)Back
        """.trimIndent()
        else """
            Yello,Wohereje ${pkg.confirmText} kuri numero $recipient
            1)Emeza
            2)Kuvamo
            0)Gusubira Inyuma
        """.trimIndent()
    )

    private fun resolveConfirm(
        choice: String,
        recipient: String,
        pkg: PkgDto,
        callerPhone: String,
        sessionId: String? = null,
        onBack: () -> String = { renderMenu("main", callerPhone) }
    ): String = when (choice) {
        "1" -> processEmeza(recipient, pkg, callerPhone, sessionId)
        "2" -> END(if (isEn(callerPhone)) "Transaction cancelled." else "Igikorwa cyahagaritswe.")
        "0" -> onBack()
        else -> invalidInput(callerPhone)
    }

    private fun processEmeza(recipient: String, pkg: PkgDto, callerPhone: String, sessionId: String? = null): String {
        val res = customerTransactionClient.processTransaction(callerPhone, recipient, pkg.label, pkg.price)
        if (res.success && !res.transactionId.isNullOrBlank() && !sessionId.isNullOrBlank()) {
            sessionStore.setTransactionId(sessionId, res.transactionId)
        }
        return END(
            if (res.success) {
                if (isEn(callerPhone))
                    "Hello, ${pkg.confirmText} sent successfully to $recipient. Thank you for using MTN."
                else
                    "Yello, ${pkg.confirmText} byagenze neza kuri $recipient. Murakoze gukoresha MTN."
            } else {
                res.message
            }
        )
    }

    private fun simplePackageFlow(
        steps: List<String>,
        fromIndex: Int,
        recipient: String,
        callerPhone: String,
        list: List<PkgDto>,
        sessionId: String? = null,
        onBack: () -> String = { renderMenu("main", callerPhone) }
    ): String {
        if (steps.size == fromIndex) return packageScreen(list, callerPhone)
        if (steps[fromIndex] == "0") return onBack()
        val pkg = pick(list, steps[fromIndex]) ?: return invalidInput(callerPhone)
        if (steps.size == fromIndex + 1) return confirmScreen(pkg, recipient, callerPhone)
        if (steps.size == fromIndex + 2) return resolveConfirm(
            steps[fromIndex + 1], recipient, pkg, callerPhone, sessionId,
            onBack = { packageScreen(list, callerPhone) }
        )
        return invalidInput(callerPhone)
    }

    private fun CON(body: String) = "CON $body"
    private fun END(body: String) = "END $body"
}
