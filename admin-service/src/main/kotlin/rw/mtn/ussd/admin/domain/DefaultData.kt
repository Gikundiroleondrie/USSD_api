package rw.mtn.ussd.admin.domain

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

data class Pkg(val label: String, val price: Int, val confirmText: String)

object DefaultMenusAndPackages {
    val me2uAmounts = listOf(
        Pkg("500Frw",   500,  "Wohereje inite za 500RWF"),
        Pkg("1,000Frw", 1000, "Wohereje inite za 1,000RWF"),
        Pkg("2,000Frw", 2000, "Wohereje inite za 2,000RWF"),
        Pkg("3,000Frw", 3000, "Wohereje inite za 3,000RWF")
    )
    val gumamo = listOf(
        Pkg("2000Frw=400Mins ku munsi/Iminsi 30 (MTN-MTN)", 2000, "2000Frw=400Mins ku munsi/Iminsi 30 (MTN-MTN)"),
        Pkg("Booster 300Frw=200Mins (Imirongo Yose)/iminsi30", 300,  "Booster 300Frw=200Mins (Imirongo Yose)/iminsi30")
    )
    val irekure24hrs = listOf(
        Pkg("200Frw=250Mins", 200, "MTN Irekure 24hrs 200Frw=250Mins"),
        Pkg("500Frw=700Mins", 500, "MTN Irekure 24hrs 500Frw=700Mins")
    )
    val irekureIcyumweru = listOf(
        Pkg("200Frw=250Mins", 200, "MTN Irekure Icyumweru 200Frw=250Mins"),
        Pkg("500Frw=700Mins", 500, "MTN Irekure Icyumweru 500Frw=700Mins")
    )
    val irekureUkwezi = listOf(
        Pkg("2000Frw=1000Mins", 2000,  "MTN Irekure Ukwezi 2000Frw=1000Mins"),
        Pkg("3000Frw=2500Mins", 3000,  "MTN Irekure Ukwezi 3000Frw=2500Mins"),
        Pkg("5000Frw=4500Mins", 5000,  "MTN Irekure Ukwezi 5000Frw=4500Mins"),
        Pkg("10000Frw=9500Mins", 10000, "MTN Irekure Ukwezi 10000Frw=9500Mins")
    )
    val voiceGwamon = listOf(
        Pkg("500Frw=800Mins/iminsi 7",         500,  "Gwamon' 500Frw=800Mins/iminsi 7"),
        Pkg("1000Frw=7GB/Iminsi 7",            1000, "Gwamon' 1000Frw=7GB/Iminsi 7"),
        Pkg("1500Frw=800Mins+8GB/Iminsi 7",    1500, "Gwamon' 1500Frw=800Mins+8GB/Iminsi 7")
    )
    val amahangaUmunsi = listOf(
        Pkg("500Rwf= 10Mins (India)",                                                      500,  "Amahanga Umunsi 500Rwf= 10Mins (India)"),
        Pkg("1000Rwf= 10Mins (Uganda, Kenya, South Sudan, Tanzania, Burundi)",             1000, "Amahanga Umunsi 1000Rwf= 10Mins (Uganda, Kenya, South Sudan, Tanzania, Burundi)"),
        Pkg("2500Rwf= 10Mins (China, France, Nigeria)",                                    2500, "Amahanga Umunsi 2500Rwf= 10Mins (China, France, Nigeria)"),
        Pkg("5000Rwf= 10Mins (Qatar, UAE, South Africa, Eritrea, Ghana, Ethiopia, Netherlands)", 5000, "Amahanga Umunsi 5000Rwf= 10Mins (Qatar, UAE, South Africa, Eritrea, Ghana, Ethiopia, Netherlands)")
    )
    val amahangaIcyumweru = listOf(
        Pkg("1000Rwf= 20Mins (India)",                                                      1000, "Amahanga Icyumweru 1000Rwf= 20Mins (India)"),
        Pkg("2000Rwf= 20Mins (Uganda, Kenya, South Sudan, Tanzania, Burundi)",              2000, "Amahanga Icyumweru 2000Rwf= 20Mins (Uganda, Kenya, South Sudan, Tanzania, Burundi)"),
        Pkg("4500Rwf= 20Mins (China, France, Nigeria)",                                     4500, "Amahanga Icyumweru 4500Rwf= 20Mins (China, France, Nigeria)"),
        Pkg("9000Rwf= 20Mins (Qatar, UAE, South Africa, Eritrea, Ghana, Ethiopia, Netherlands)", 9000, "Amahanga Icyumweru 9000Rwf= 20Mins (Qatar, UAE, South Africa, Eritrea, Ghana, Ethiopia, Netherlands)")
    )
    val amahangaUkwezi = listOf(
        Pkg("2500Rwf= 60Mins (India)",                                                       2500,  "Amahanga Ukwezi 2500Rwf= 60Mins (India)"),
        Pkg("5400Rwf= 60Mins (Uganda, Kenya, South Sudan, Tanzania, Burundi)",               5400,  "Amahanga Ukwezi 5400Rwf= 60Mins (Uganda, Kenya, South Sudan, Tanzania, Burundi)"),
        Pkg("13000Rwf= 60Mins (China, France, Nigeria)",                                     13000, "Amahanga Ukwezi 13000Rwf= 60Mins (China, France, Nigeria)"),
        Pkg("24000Rwf= 60Mins (Qatar, UAE, South Africa, Eritrea, Ghana, Ethiopia, Netherlands)", 24000, "Amahanga Ukwezi 24000Rwf= 60Mins (Qatar, UAE, South Africa, Eritrea, Ghana, Ethiopia, Netherlands)")
    )
    val desade = listOf(
        Pkg("200Frw=200Mins/48hrs",        200, "DesaDe 200Frw=200Mins/48hrs"),
        Pkg("200Frw=100Mins+100MBs/48hrs", 200, "DesaDe 200Frw=100Mins+100MBs/48hrs")
    )
    val voiceFoLeva = listOf(
        Pkg("5000Frw=10GB+1000Mins",   5000,  "FoLeva 5000Frw=10GB+1000Mins"),
        Pkg("10000Frw=25GB+2500Mins",  10000, "FoLeva 10000Frw=25GB+2500Mins"),
        Pkg("20000Frw=75GB+3000Mins",  20000, "FoLeva 20000Frw=75GB+3000Mins")
    )
    val tubitayeho = listOf(
        Pkg("3000Frw=15GB+200SMS",                  3000,  "Tubitayeho 3000Frw=15GB+200SMS"),
        Pkg("5000Frw=30GB+200SMS",                  5000,  "Tubitayeho 5000Frw=30GB+200SMS"),
        Pkg("10000Frw=60GB+150Mins/day+200SMS",     10000, "Tubitayeho 10000Frw=60GB+150Mins/day+200SMS")
    )
    val internetIrekureUmunsi = listOf(
        Pkg("1000Frw=2.2GB", 1000, "Internet Irekure Umunsi 1000Frw=2.2GB"),
        Pkg("500Frw=1.5GB",  500,  "Internet Irekure Umunsi 500Frw=1.5GB")
    )
    val internetIrekureIcyumweru = listOf(
        Pkg("5000Frw=10GB", 5000, "Internet Irekure Icyumweru 5000Frw=10GB"),
        Pkg("2000Frw=3GB",  2000, "Internet Irekure Icyumweru 2000Frw=3GB"),
        Pkg("1000Frw=1GB",  1000, "Internet Irekure Icyumweru 1000Frw=1GB")
    )
    val internetIrekureUkwezi = listOf(
        Pkg("10000Frw=30720MB", 10000, "Internet Irekure Ukwezi 10000Frw=30720MB"),
        Pkg("5000Frw=7168MB",   5000,  "Internet Irekure Ukwezi 5000Frw=7168MB"),
        Pkg("2000Frw=2048MB",   2000,  "Internet Irekure Ukwezi 2000Frw=2048MB")
    )
    val dataGwamon = listOf(
        Pkg("5000Frw=10GB+1000Mins",  5000,  "Gwamon' 5000Frw=10GB+1000Mins"),
        Pkg("10000Frw=25GB+2500Mins", 10000, "Gwamon' 10000Frw=25GB+2500Mins"),
        Pkg("20000Frw=75GB+3000Mins", 20000, "Gwamon' 20000Frw=75GB+3000Mins")
    )
    val dataFoLeva = listOf(
        Pkg("500Frw=800Mins/iminsi7",       500,  "FoLeva 500Frw=800Mins/iminsi7"),
        Pkg("1000Frw=7GB/Iminsi7",          1000, "FoLeva 1000Frw=7GB/Iminsi7"),
        Pkg("1500Frw=800Mins+8GB/Iminsi7",  1500, "FoLeva 1500Frw=800Mins+8GB/Iminsi7")
    )
    val socialMediaWhatsapp = listOf(
        Pkg("200Frw=510MBs/24hrs", 200, "Whatsapp 200Frw=510MBs/24hrs")
    )
    val socialMediaFacebook = listOf(
        Pkg("200Frw=810MBs/24hrs", 200, "Facebook na Instagram 200Frw=810MBs/24hrs")
    )
    val prestige = listOf(
        Pkg("5000Rwf=1000Mins+10GB/30days",    5000,  "Prestige 5000Rwf=1000Mins+10GB/30days"),
        Pkg("10000Rwf=2500Mins+25GB/30days",   10000, "Prestige 10000Rwf=2500Mins+25GB/30days"),
        Pkg("20000Rwf=3000Mins+75GB/30days",   20000, "Prestige 20000Rwf=3000Mins+75GB/30days"),
        Pkg("50000Rwf=10000Mins+225GB/30days", 50000, "Prestige 50000Rwf=10000Mins+225GB/30days")
    )
}

@Component
class DataLoader(
    private val repo: PackageRepository,
    private val menuRepo: MenuRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        seedPackages()
        seedMenus()
    }

    private fun seedPackages() {
        if (repo.count() > 0L) return

        val seed: Map<String, List<Pkg>> = linkedMapOf(
            "me2u"                    to DefaultMenusAndPackages.me2uAmounts,
            "gumamo"                  to DefaultMenusAndPackages.gumamo,
            "irekure24hrs"            to DefaultMenusAndPackages.irekure24hrs,
            "irekureIcyumweru"        to DefaultMenusAndPackages.irekureIcyumweru,
            "irekureUkwezi"           to DefaultMenusAndPackages.irekureUkwezi,
            "voiceGwamon"             to DefaultMenusAndPackages.voiceGwamon,
            "amahangaUmunsi"          to DefaultMenusAndPackages.amahangaUmunsi,
            "amahangaIcyumweru"       to DefaultMenusAndPackages.amahangaIcyumweru,
            "amahangaUkwezi"          to DefaultMenusAndPackages.amahangaUkwezi,
            "desade"                  to DefaultMenusAndPackages.desade,
            "voiceFoLeva"             to DefaultMenusAndPackages.voiceFoLeva,
            "tubitayeho"              to DefaultMenusAndPackages.tubitayeho,
            "internetIrekureUmunsi"   to DefaultMenusAndPackages.internetIrekureUmunsi,
            "internetIrekureIcyumweru" to DefaultMenusAndPackages.internetIrekureIcyumweru,
            "internetIrekureUkwezi"   to DefaultMenusAndPackages.internetIrekureUkwezi,
            "dataGwamon"              to DefaultMenusAndPackages.dataGwamon,
            "dataFoLeva"              to DefaultMenusAndPackages.dataFoLeva,
            "socialWhatsapp"          to DefaultMenusAndPackages.socialMediaWhatsapp,
            "socialFacebook"          to DefaultMenusAndPackages.socialMediaFacebook,
            "prestige"                to DefaultMenusAndPackages.prestige
        )

        val entities = seed.flatMap { (menuKey, pkgs) ->
            pkgs.mapIndexed { index, pkg ->
                PackageEntity(
                    menuKey     = menuKey,
                    sortOrder   = index + 1,
                    label       = pkg.label,
                    price       = pkg.price,
                    confirmText = pkg.confirmText,
                    active      = true
                )
            }
        }
        repo.saveAll(entities)
    }

    private fun seedMenus() {
        if (menuRepo.count() > 0L) return

        val items = listOf(
            menu("main", 1, "Kohereza Me2U",      "Send Me2U",           "me2u",           requiresPhone = true),
            menu("main", 2, "Voice pack",          "Voice pack",          "voicepack",      requiresPhone = true),
            menu("main", 3, "Bundle za Internet",  "Internet bundles",    "bundleinternet", requiresPhone = true),
            menu("main", 4, "Prestige",            "Prestige",            "prestige",       requiresPhone = true),
            menu("main", 5, "Hindura Ururimi",     "Change Language",     "changelanguage", requiresPhone = false),

            menu("voicepack", 1, "Gumamo",              "Gumamo",                "gumamo",           requiresPhone = false),
            menu("voicepack", 2, "MTN Irekure 24hrs",   "MTN Irekure 24hrs",     "irekure24hrs",     requiresPhone = false),
            menu("voicepack", 3, "MTN Irekure Icyumweru","MTN Irekure Weekly",   "irekureIcyumweru", requiresPhone = false),
            menu("voicepack", 4, "MTN Irekure Ukwezi",  "MTN Irekure Monthly",   "irekureUkwezi",    requiresPhone = false),
            menu("voicepack", 5, "Gwamon'",             "Gwamon'",               "voiceGwamon",      requiresPhone = false),
            menu("voicepack", 6, "Amahanga",            "International",         "amahanga",         requiresPhone = false),
            menu("voicepack", 7, "DesaDe",              "DesaDe",                "desade",           requiresPhone = false),
            menu("voicepack", 8, "FoLeva",              "FoLeva",                "voiceFoLeva",      requiresPhone = false),

            menu("amahanga", 1, "Umunsi",    "Daily",   "amahangaUmunsi",    requiresPhone = false),
            menu("amahanga", 2, "Icyumweru", "Weekly",  "amahangaIcyumweru", requiresPhone = false),
            menu("amahanga", 3, "Ukwezi",    "Monthly", "amahangaUkwezi",    requiresPhone = false),

            menu("bundleinternet", 1, "Tubitayeho",           "Tubitayeho",          "tubitayeho",    requiresPhone = false),
            menu("bundleinternet", 2, "Internet Irekure",     "Internet Irekure",    "internetirekure",requiresPhone = false),
            menu("bundleinternet", 3, "Gwamon'",              "Gwamon'",             "dataGwamon",    requiresPhone = false),
            menu("bundleinternet", 4, "FoLeva",               "FoLeva",              "dataFoLeva",    requiresPhone = false),
            menu("bundleinternet", 5, "Bundle za Social Media","Social Media bundles","socialmedia",   requiresPhone = false),
            menu("bundleinternet", 6, "Router Bundles",       "Router Bundles",      "routerbundles", requiresPhone = false),

            menu("internetirekure", 1, "Umunsi",    "Daily",   "internetIrekureUmunsi",    requiresPhone = false),
            menu("internetirekure", 2, "Icyumweru", "Weekly",  "internetIrekureIcyumweru", requiresPhone = false),
            menu("internetirekure", 3, "Ukwezi",    "Monthly", "internetIrekureUkwezi",    requiresPhone = false),

            menu("socialmedia", 1, "Whatsapp",              "Whatsapp",              "socialWhatsapp",  requiresPhone = false),
            menu("socialmedia", 2, "Facebook na Instagram", "Facebook & Instagram",  "socialFacebook",  requiresPhone = false),

            menu("prestigemenu", 1, "Prestige Packs(Ukwezi)", "Prestige Packs (Monthly)", "prestige", requiresPhone = false)
        )

        menuRepo.saveAll(items)
    }

    private fun menu(
        menuKey: String,
        position: Int,
        labelRw: String,
        labelEn: String,
        targetKey: String,
        requiresPhone: Boolean
    ) = MenuEntity(
        menuKey      = menuKey,
        position     = position,
        labelRw      = labelRw,
        labelEn      = labelEn,
        targetKey    = targetKey,
        requiresPhone = requiresPhone,
        active       = true
    )
}
