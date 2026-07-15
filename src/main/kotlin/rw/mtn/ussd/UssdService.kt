package rw.mtn.ussd

import org.springframework.stereotype.Service
import rw.mtn.ussd.entity.MenuEntity
import rw.mtn.ussd.repository.MenuRepository
import rw.mtn.ussd.repository.PackageRepository

@Service
class UssdService(
    private val packages: PackageService,
    private val menuRepo: MenuRepository,
    private val customerRepository: rw.mtn.ussd.customer.CustomerRepository,
    private val transactionRepository: rw.mtn.ussd.transaction.TransactionRepository
) {
    private val phoneRegex = Regex("^07\\d{8}$")
    private val languagePrefs = mutableMapOf<String, String>()
    private val sessionPaths = mutableMapOf<String, MutableList<String>>()

    private fun isEn(phone: String) = languagePrefs[phone] == "en"

    // ── Session management ────────────────────────────────────────────────────

    fun processInput(sessionId: String, phoneNumber: String, rawText: String): String {
        registerCustomerIfNew(phoneNumber)

        val path = sessionPaths.getOrPut(sessionId) { mutableListOf() }
        val newInput = if (rawText.isBlank()) "" else rawText.split("*").last()

        when {
            newInput.isBlank() -> path.clear()
            newInput == "0" -> {
                if (path.isNotEmpty()) path.removeLast()
                if (path.isNotEmpty()) path.removeLast()
            }
            else -> path.add(newInput)
        }

        val replayText = path.joinToString("*")
        val response = handle(phoneNumber, replayText)
        if (response.startsWith("END")) sessionPaths.remove(sessionId)
        return response
    }

    fun clearSession(sessionId: String) = sessionPaths.remove(sessionId)

    // ── Core handler ──────────────────────────────────────────────────────────

    fun handle(phoneNumber: String, text: String): String {
        val steps = if (text.isBlank()) emptyList() else text.split("*")
        if (steps.isEmpty()) return renderMenu("main", phoneNumber)

        // Step 0 — main menu selection
        val mainItems = menuRepo.findByMenuKeyAndActiveTrueOrderByPositionAsc("main")
        if (mainItems.isEmpty()) return END("MTN Rwandacell Message\nService unavailable")

        val mainChoice = steps[0].toIntOrNull()
        if (mainChoice == null || mainChoice < 1 || mainChoice > mainItems.size) {
            return invalidInput(phoneNumber)
        }

        val mainItem = mainItems[mainChoice - 1]

        // Handle Change Language (no phone, no packages — just toggle)
        if (mainItem.targetKey == "changelanguage") {
            languagePrefs[phoneNumber] = if (isEn(phoneNumber)) "rw" else "en"
            return renderMenu("main", phoneNumber)
        }

        // All other main items require a phone number
        if (steps.size == 1) return phonePrompt(phoneNumber)
        val recipient = validatedPhone(steps[1]) ?: return invalidPhone(phoneNumber)

        return resolveTarget(mainItem.targetKey, steps, 2, recipient, phoneNumber)
    }

    // ── Target resolver — routes to sub-menu or package list ─────────────────

    private fun resolveTarget(
        targetKey: String,
        steps: List<String>,
        fromIndex: Int,
        recipient: String,
        callerPhone: String
    ): String {
        // Special cases that don't have DB menu items
        return when (targetKey) {
            "routerbundles" -> END(
                if (isEn(callerPhone)) "MTN Rwanda Message\nComing soon"
                else "MTN Rwandacell Message\nComing soon"
            )
            else -> {
                // Check if targetKey is a sub-menu (has children in menus table)
                val subItems = menuRepo.findByMenuKeyAndActiveTrueOrderByPositionAsc(targetKey)
                if (subItems.isNotEmpty()) {
                    // It's a sub-menu — render it or process choice
                    handleSubMenu(targetKey, subItems, steps, fromIndex, recipient, callerPhone)
                } else {
                    // It's a package list — run the package flow
                    simplePackageFlow(
                        steps, fromIndex, recipient, callerPhone,
                        packages.get(targetKey),
                        onBack = { renderMenu(parentOf(targetKey), callerPhone) }
                    )
                }
            }
        }
    }

    private fun handleSubMenu(
        menuKey: String,
        items: List<MenuEntity>,
        steps: List<String>,
        fromIndex: Int,
        recipient: String,
        callerPhone: String
    ): String {
        if (steps.size == fromIndex) return renderMenu(menuKey, callerPhone, items)

        val choice = steps[fromIndex]
        if (choice == "0") return renderMenu(parentOf(menuKey), callerPhone)

        val idx = choice.toIntOrNull()
        if (idx == null || idx < 1 || idx > items.size) return invalidInput(callerPhone)

        val chosen = items[idx - 1]
        return resolveTarget(chosen.targetKey, steps, fromIndex + 1, recipient, callerPhone)
    }

    // ── Menu rendering ────────────────────────────────────────────────────────

    private fun renderMenu(menuKey: String, phone: String, items: List<MenuEntity>? = null): String {
        val list = items ?: menuRepo.findByMenuKeyAndActiveTrueOrderByPositionAsc(menuKey)
        if (list.isEmpty()) return END("MTN Rwandacell Message\nService unavailable")

        val lines = list.mapIndexed { i, item ->
            "${i + 1})${if (isEn(phone)) item.labelEn else item.labelRw}"
        }
        val back = if (menuKey != "main") listOf(backOption(phone)) else emptyList()
        return CON((lines + back).joinToString("\n"))
    }

    // ── Parent menu lookup — used for back navigation ─────────────────────────
    // Maps each sub-menu key to its parent so "0" knows where to go back to

    private fun parentOf(targetKey: String): String = when (targetKey) {
        "voicepack"       -> "main"
        "bundleinternet"  -> "main"
        "prestige"        -> "main"
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
        "prestige"       -> "prestigemenu"
        else             -> "main"
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun registerCustomerIfNew(phoneNumber: String) {
        val normalized = phoneNumber.removePrefix("+250").let {
            if (it.startsWith("7")) "0$it" else it
        }
        customerRepository.findByPhoneNumber(normalized)
            ?: customerRepository.save(
                rw.mtn.ussd.customer.CustomerEntity(phoneNumber = normalized, active = true)
            )
    }

    private fun invalidInput(phone: String) =
        END(if (isEn(phone)) "MTN Rwanda Message\nInvalid input" else "MTN Rwandacell Message\nInvalid input")

    private fun invalidPhone(phone: String) =
        END(if (isEn(phone)) "MTN Rwanda Message\nInvalid recipient number" else "MTN Rwandacell Message\nUshyizemo numero yo kohereza itariyo")

    private fun insufficientBalance(phone: String) =
        END(
            if (isEn(phone)) "MTN Rwanda Message\nInsufficient balance. Please top up."
            else "MTN Rwandacell Message\nMukiriya wacu, kohereza Me2U byanze kuko mufite inite zidahagije. Mushyiremo inite"
        )

    private fun phonePrompt(phone: String) =
        CON(if (isEn(phone)) "Enter recipient number (07xxxxxxxx)" else "Shyiramo numero yo kohereza inite(07xxxxxxxx)")

    private fun validatedPhone(raw: String): String? =
        if (phoneRegex.matches(raw)) raw else null

    private fun backOption(phone: String) =
        if (isEn(phone)) "0)Back" else "0)Gusubira Inyuma"

    private fun packageScreen(list: List<Pkg>, phone: String): String {
        val lines = list.mapIndexed { i, pkg -> "${i + 1})${pkg.label}" }
        return CON((lines + backOption(phone)).joinToString("\n"))
    }

    private fun pick(list: List<Pkg>, choice: String): Pkg? =
        choice.toIntOrNull()?.let { list.getOrNull(it - 1) }

    private fun confirmScreen(pkg: Pkg, recipient: String, phone: String): String = CON(
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
        pkg: Pkg,
        callerPhone: String,
        onBack: () -> String = { renderMenu("main", callerPhone) }
    ): String = when (choice) {
        "1" -> processEmeza(recipient, pkg, callerPhone)
        "2" -> END(if (isEn(callerPhone)) "Transaction cancelled." else "Igikorwa cyahagaritswe.")
        "0" -> onBack()
        else -> invalidInput(callerPhone)
    }

    private fun processEmeza(recipient: String, pkg: Pkg, callerPhone: String): String {
        val balance = SimulatedAccounts.balanceOf(callerPhone)
        return if (balance >= pkg.price) {
            SimulatedAccounts.debit(callerPhone, pkg.price)
            val customer = customerRepository.findByPhoneNumber(
                callerPhone.removePrefix("+250").let { if (it.startsWith("7")) "0$it" else it }
            )
            if (customer != null) {
                transactionRepository.save(
                    rw.mtn.ussd.transaction.TransactionEntity(
                        customer = customer,
                        receiverNumber = recipient,
                        packageLabel = pkg.label,
                        price = pkg.price
                    )
                )
            }
            END(
                if (isEn(callerPhone))
                    "Hello, ${pkg.confirmText} sent successfully to $recipient. Thank you for using MTN."
                else
                    "Yello, ${pkg.confirmText} byagenze neza kuri $recipient. Murakoze gukoresha MTN."
            )
        } else insufficientBalance(callerPhone)
    }

    private fun simplePackageFlow(
        steps: List<String>,
        fromIndex: Int,
        recipient: String,
        callerPhone: String,
        list: List<Pkg>,
        onBack: () -> String = { renderMenu("main", callerPhone) }
    ): String {
        if (steps.size == fromIndex) return packageScreen(list, callerPhone)
        if (steps[fromIndex] == "0") return onBack()
        val pkg = pick(list, steps[fromIndex]) ?: return invalidInput(callerPhone)
        if (steps.size == fromIndex + 1) return confirmScreen(pkg, recipient, callerPhone)
        if (steps.size == fromIndex + 2) return resolveConfirm(
            steps[fromIndex + 1], recipient, pkg, callerPhone,
            onBack = { packageScreen(list, callerPhone) }
        )
        return invalidInput(callerPhone)
    }

    private fun CON(body: String) = "CON $body"
    private fun END(body: String) = "END $body"
}
