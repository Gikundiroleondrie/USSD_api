package rw.mtn.ussd

import org.springframework.stereotype.Service
import rw.mtn.ussd.repository.PackageRepository

@Service
class PackageService(private val repo: PackageRepository) {

    fun get(menuKey: String): List<Pkg> =
        repo.findByMenuKeyAndActiveTrueOrderBySortOrderAsc(menuKey).map {
            Pkg(label = it.label, price = it.price, confirmText = it.confirmText)
        }
}
