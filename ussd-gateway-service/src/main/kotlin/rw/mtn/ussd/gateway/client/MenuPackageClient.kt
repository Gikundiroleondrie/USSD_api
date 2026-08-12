package rw.mtn.ussd.gateway.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod

data class MenuDto(
    val id: Long? = null,
    val menuKey: String = "",
    val position: Int = 0,
    val labelEn: String = "",
    val labelRw: String = "",
    val targetKey: String = "",
    val requiresPhone: Boolean = false,
    val active: Boolean = true
)

data class PkgDto(
    val label: String,
    val price: Int,
    val confirmText: String
)

@Component
class MenuPackageClient(
    @Value("\${services.admin.url:http://localhost:8083}") private val adminUrl: String,
    private val restTemplate: RestTemplate = RestTemplate()
) {

    private val fallbackMenus: Map<String, List<MenuDto>> = mapOf(
        "main" to listOf(
            MenuDto(1, "main", 1, "Send Me2U", "Kohereza Me2U", "me2u", requiresPhone = true),
            MenuDto(2, "main", 2, "Voice pack", "Voice pack", "voicepack", requiresPhone = true),
            MenuDto(3, "main", 3, "Internet bundles", "Bundle za Internet", "bundleinternet", requiresPhone = true),
            MenuDto(4, "main", 4, "Prestige", "Prestige", "prestige", requiresPhone = true),
            MenuDto(5, "main", 5, "Change Language", "Hindura Ururimi", "changelanguage", requiresPhone = false)
        ),
        "voicepack" to listOf(
            MenuDto(6, "voicepack", 1, "Gumamo", "Gumamo", "gumamo", requiresPhone = false),
            MenuDto(7, "voicepack", 2, "MTN Irekure 24hrs", "MTN Irekure 24hrs", "irekure24hrs", requiresPhone = false),
            MenuDto(8, "voicepack", 3, "MTN Irekure Weekly", "MTN Irekure Icyumweru", "irekureIcyumweru", requiresPhone = false),
            MenuDto(9, "voicepack", 4, "MTN Irekure Monthly", "MTN Irekure Ukwezi", "irekureUkwezi", requiresPhone = false),
            MenuDto(10, "voicepack", 5, "Gwamon'", "Gwamon'", "voiceGwamon", requiresPhone = false),
            MenuDto(11, "voicepack", 6, "International", "Amahanga", "amahanga", requiresPhone = false),
            MenuDto(12, "voicepack", 7, "DesaDe", "DesaDe", "desade", requiresPhone = false),
            MenuDto(13, "voicepack", 8, "FoLeva", "FoLeva", "voiceFoLeva", requiresPhone = false)
        ),
        "amahanga" to listOf(
            MenuDto(14, "amahanga", 1, "Daily", "Umunsi", "amahangaUmunsi", requiresPhone = false),
            MenuDto(15, "amahanga", 2, "Weekly", "Icyumweru", "amahangaIcyumweru", requiresPhone = false),
            MenuDto(16, "amahanga", 3, "Monthly", "Ukwezi", "amahangaUkwezi", requiresPhone = false)
        ),
        "bundleinternet" to listOf(
            MenuDto(17, "bundleinternet", 1, "Tubitayeho", "Tubitayeho", "tubitayeho", requiresPhone = false),
            MenuDto(18, "bundleinternet", 2, "Internet Irekure", "Internet Irekure", "internetirekure", requiresPhone = false),
            MenuDto(19, "bundleinternet", 3, "Gwamon'", "Gwamon'", "dataGwamon", requiresPhone = false),
            MenuDto(20, "bundleinternet", 4, "FoLeva", "FoLeva", "dataFoLeva", requiresPhone = false),
            MenuDto(21, "bundleinternet", 5, "Social Media bundles", "Bundle za Social Media", "socialmedia", requiresPhone = false),
            MenuDto(22, "bundleinternet", 6, "Router Bundles", "Router Bundles", "routerbundles", requiresPhone = false)
        ),
        "internetirekure" to listOf(
            MenuDto(23, "internetirekure", 1, "Daily", "Umunsi", "internetIrekureUmunsi", requiresPhone = false),
            MenuDto(24, "internetirekure", 2, "Weekly", "Icyumweru", "internetIrekureIcyumweru", requiresPhone = false),
            MenuDto(25, "internetirekure", 3, "Monthly", "Ukwezi", "internetIrekureUkwezi", requiresPhone = false)
        ),
        "socialmedia" to listOf(
            MenuDto(26, "socialmedia", 1, "Whatsapp", "Whatsapp", "socialWhatsapp", requiresPhone = false),
            MenuDto(27, "socialmedia", 2, "Facebook & Instagram", "Facebook na Instagram", "socialFacebook", requiresPhone = false)
        ),
        "prestige" to listOf(
            MenuDto(28, "prestige", 1, "Prestige Packs (Monthly)", "Prestige Packs(Ukwezi)", "prestigepackages", requiresPhone = false)
        )
    )

    private val fallbackPackages: Map<String, List<PkgDto>> = mapOf(
        "me2u" to listOf(
            PkgDto("500Frw", 500, "Wohereje inite za 500RWF"),
            PkgDto("1,000Frw", 1000, "Wohereje inite za 1,000RWF"),
            PkgDto("2,000Frw", 2000, "Wohereje inite za 2,000RWF"),
            PkgDto("3,000Frw", 3000, "Wohereje inite za 3,000RWF")
        ),
        "gumamo" to listOf(
            PkgDto("2000Frw=400Mins ku munsi/Iminsi 30 (MTN-MTN)", 2000, "2000Frw=400Mins ku munsi/Iminsi 30 (MTN-MTN)"),
            PkgDto("Booster 300Frw=200Mins (Imirongo Yose)/iminsi30", 300, "Booster 300Frw=200Mins (Imirongo Yose)/iminsi30")
        ),
        "irekure24hrs" to listOf(
            PkgDto("200Frw=250Mins", 200, "MTN Irekure 24hrs 200Frw=250Mins"),
            PkgDto("500Frw=700Mins", 500, "MTN Irekure 24hrs 500Frw=700Mins")
        ),
        "irekureIcyumweru" to listOf(
            PkgDto("200Frw=250Mins", 200, "MTN Irekure Icyumweru 200Frw=250Mins"),
            PkgDto("500Frw=700Mins", 500, "MTN Irekure Icyumweru 500Frw=700Mins")
        ),
        "irekureUkwezi" to listOf(
            PkgDto("2000Frw=1000Mins", 2000, "MTN Irekure Ukwezi 2000Frw=1000Mins"),
            PkgDto("3000Frw=2500Mins", 3000, "MTN Irekure Ukwezi 3000Frw=2500Mins")
        ),
        "voiceGwamon" to listOf(
            PkgDto("500Frw=800Mins/iminsi 7", 500, "Gwamon' 500Frw=800Mins/iminsi 7"),
            PkgDto("1000Frw=7GB/Iminsi 7", 1000, "Gwamon' 1000Frw=7GB/Iminsi 7")
        ),
        "prestige" to listOf(
            PkgDto("5000Rwf=1000Mins+10GB/30days", 5000, "Prestige 5000Rwf=1000Mins+10GB/30days"),
            PkgDto("10000Rwf=2500Mins+25GB/30days", 10000, "Prestige 10000Rwf=2500Mins+25GB/30days")
        )
    )

    fun getMenuItems(menuKey: String): List<MenuDto> {
        return try {
            val response = restTemplate.exchange(
                "$adminUrl/api/menu-package/menus/$menuKey",
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<MenuDto>>() {}
            )
            val resList = response.body
            if (!resList.isNullOrEmpty()) resList else (fallbackMenus[menuKey] ?: emptyList())
        } catch (e: Exception) {
            fallbackMenus[menuKey] ?: emptyList()
        }
    }

    fun getPackages(menuKey: String): List<PkgDto> {
        return try {
            val response = restTemplate.exchange(
                "$adminUrl/api/menu-package/packages/$menuKey",
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<PkgDto>>() {}
            )
            val resList = response.body
            if (!resList.isNullOrEmpty()) resList else (fallbackPackages[menuKey] ?: emptyList())
        } catch (e: Exception) {
            fallbackPackages[menuKey] ?: emptyList()
        }
    }
}
