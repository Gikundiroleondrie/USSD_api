package rw.mtn.ussd.config

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import rw.mtn.ussd.Menus
import rw.mtn.ussd.Pkg
import rw.mtn.ussd.entity.MenuEntity
import rw.mtn.ussd.entity.PackageEntity
import rw.mtn.ussd.repository.MenuRepository
import rw.mtn.ussd.repository.PackageRepository

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
            "me2u"                    to Menus.me2uAmounts,
            "gumamo"                  to Menus.gumamo,
            "irekure24hrs"            to Menus.irekure24hrs,
            "irekureIcyumweru"        to Menus.irekureIcyumweru,
            "irekureUkwezi"           to Menus.irekureUkwezi,
            "voiceGwamon"             to Menus.voiceGwamon,
            "amahangaUmunsi"          to Menus.amahangaUmunsi,
            "amahangaIcyumweru"       to Menus.amahangaIcyumweru,
            "amahangaUkwezi"          to Menus.amahangaUkwezi,
            "desade"                  to Menus.desade,
            "voiceFoLeva"             to Menus.voiceFoLeva,
            "tubitayeho"              to Menus.tubitayeho,
            "internetIrekureUmunsi"   to Menus.internetIrekureUmunsi,
            "internetIrekureIcyumweru" to Menus.internetIrekureIcyumweru,
            "internetIrekureUkwezi"   to Menus.internetIrekureUkwezi,
            "dataGwamon"              to Menus.dataGwamon,
            "dataFoLeva"              to Menus.dataFoLeva,
            "socialWhatsapp"          to Menus.socialMediaWhatsapp,
            "socialFacebook"          to Menus.socialMediaFacebook,
            "prestige"                to Menus.prestige
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
        println("Seeded ${entities.size} packages")
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
        println("Seeded ${items.size} menu items")
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
