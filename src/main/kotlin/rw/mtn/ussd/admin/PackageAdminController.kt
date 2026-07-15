package rw.mtn.ussd.admin

import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import rw.mtn.ussd.entity.PackageEntity
import rw.mtn.ussd.repository.PackageRepository

data class PackageRequest(
    val menuKey: String,
    val sortOrder: Int,
    val label: String,
    val price: Int,
    val confirmText: String,
    val active: Boolean = true
)

@RestController
@RequestMapping("/admin/packages")
class PackageAdminController(private val repo: PackageRepository) {

    @GetMapping
    fun list(@RequestParam(required = false) menuKey: String?): List<PackageEntity> =
        if (menuKey.isNullOrBlank()) {
            repo.findAll().sortedWith(compareBy({ it.menuKey }, { it.sortOrder }))
        } else {
            repo.findByMenuKeyOrderBySortOrderAsc(menuKey)
        }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<PackageEntity> =
        repo.findById(id).map { ResponseEntity.ok(it) }.orElse(ResponseEntity.notFound().build())

    @PostMapping
    @Transactional
    fun create(@RequestBody req: PackageRequest): PackageEntity {
        val saved = repo.save(
            PackageEntity(
                menuKey     = req.menuKey,
                sortOrder   = req.sortOrder,
                label       = req.label,
                price       = req.price,
                confirmText = req.confirmText,
                active      = req.active
            )
        )
        reorder(req.menuKey)
        return saved
    }

    @PutMapping("/{id}")
    @Transactional
    fun update(@PathVariable id: Long, @RequestBody req: PackageRequest): ResponseEntity<PackageEntity> {
        val existing = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val updated = repo.save(
            existing.copy(
                menuKey     = req.menuKey,
                sortOrder   = req.sortOrder,
                label       = req.label,
                price       = req.price,
                confirmText = req.confirmText,
                active      = req.active
            )
        )
        reorder(req.menuKey)
        return ResponseEntity.ok(updated)
    }
    
    @PatchMapping("/{id}/toggle")
    @Transactional
    fun toggle(@PathVariable id: Long): ResponseEntity<PackageEntity> {
        val existing = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val toggled = repo.save(existing.copy(active = !existing.active))
        reorder(existing.menuKey)
        return ResponseEntity.ok(toggled)
    }

    
    @DeleteMapping("/{id}")
    @Transactional
    fun delete(@PathVariable id: Long): ResponseEntity<Any> {
        val existing = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val menuKey = existing.menuKey
        repo.deleteById(id)
        reorder(menuKey)
        return ResponseEntity.ok(mapOf(
            "message" to "Package deleted. Remaining packages in '$menuKey' reordered."
        ))
    }

    @PostMapping("/{menuKey}/reorder")
    @Transactional
    fun reorderEndpoint(@PathVariable menuKey: String): ResponseEntity<Any> {
        reorder(menuKey)
        return ResponseEntity.ok(mapOf("message" to "Reordered active packages for '$menuKey'"))
    }

    private fun reorder(menuKey: String) {
        val active = repo.findByMenuKeyAndActiveTrueOrderBySortOrderAsc(menuKey)
        active.forEachIndexed { index, pkg ->
            val newOrder = index + 1
            if (pkg.sortOrder != newOrder) {
                repo.save(pkg.copy(sortOrder = newOrder))
            }
        }
    }
}