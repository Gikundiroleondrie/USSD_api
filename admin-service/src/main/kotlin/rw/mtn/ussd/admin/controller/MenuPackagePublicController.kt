package rw.mtn.ussd.admin.controller

import org.springframework.web.bind.annotation.*
import rw.mtn.ussd.admin.domain.*

@RestController
@RequestMapping("/api/menu-package")
class MenuPackagePublicController(
    private val menuRepo: MenuRepository,
    private val packageRepo: PackageRepository
) {

    @GetMapping("/menus/{menuKey}")
    fun getMenus(@PathVariable menuKey: String): List<MenuEntity> {
        return menuRepo.findByMenuKeyAndActiveTrueOrderByPositionAsc(menuKey)
    }

    @GetMapping("/packages/{menuKey}")
    fun getPackages(@PathVariable menuKey: String): List<PackageEntity> {
        return packageRepo.findByMenuKeyAndActiveTrueOrderBySortOrderAsc(menuKey)
    }
}
