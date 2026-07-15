package rw.mtn.ussd.admin

import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import rw.mtn.ussd.entity.MenuEntity
import rw.mtn.ussd.repository.MenuRepository

data class MenuRequest(
    val menuKey: String,
    val position: Int,
    val labelRw: String,
    val labelEn: String,
    val targetKey: String,
    val requiresPhone: Boolean = false,
    val active: Boolean = true
)

@RestController
@RequestMapping("/admin/menus")
class MenuAdminController(private val menuRepo: MenuRepository) {

    
    @GetMapping
    fun list(@RequestParam(required = false) menuKey: String?): List<MenuEntity> =
        if (menuKey.isNullOrBlank()) {
            menuRepo.findAll().sortedWith(compareBy({ it.menuKey }, { it.position }))
        } else {
            menuRepo.findByMenuKeyOrderByPositionAsc(menuKey)
        }

    
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<MenuEntity> =
        menuRepo.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    
    @PostMapping
    @Transactional
    fun create(@RequestBody req: MenuRequest): MenuEntity {
        val saved = menuRepo.save(
            MenuEntity(
                menuKey       = req.menuKey,
                position      = req.position,
                labelRw       = req.labelRw,
                labelEn       = req.labelEn,
                targetKey     = req.targetKey,
                requiresPhone = req.requiresPhone,
                active        = req.active
            )
        )
        reorder(req.menuKey)
        return saved
    }

    
    @PutMapping("/{id}")
    @Transactional
    fun update(@PathVariable id: Long, @RequestBody req: MenuRequest): ResponseEntity<MenuEntity> {
        val existing = menuRepo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val updated = menuRepo.save(
            existing.copy(
                menuKey       = req.menuKey,
                position      = req.position,
                labelRw       = req.labelRw,
                labelEn       = req.labelEn,
                targetKey     = req.targetKey,
                requiresPhone = req.requiresPhone,
                active        = req.active
            )
        )
        reorder(req.menuKey)
        return ResponseEntity.ok(updated)
    }

    
    @PatchMapping("/{id}/toggle")
    @Transactional
    fun toggle(@PathVariable id: Long): ResponseEntity<MenuEntity> {
        val existing = menuRepo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val toggled = menuRepo.save(existing.copy(active = !existing.active))
        reorder(existing.menuKey)
        return ResponseEntity.ok(toggled)
    }


    @DeleteMapping("/{id}")
    @Transactional
    fun delete(@PathVariable id: Long): ResponseEntity<Any> {
        val existing = menuRepo.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        val deleted = mutableListOf<String>()
        deleteCascade(existing, deleted)
        reorder(existing.menuKey)

        return ResponseEntity.ok(mapOf(
            "message" to "Deleted menu item and all its children. Remaining items reordered.",
            "deletedMenuKeys" to deleted
        ))
    }

    @PostMapping("/{menuKey}/reorder")
    @Transactional
    fun reorderEndpoint(@PathVariable menuKey: String): ResponseEntity<Any> {
        reorder(menuKey)
        return ResponseEntity.ok(mapOf("message" to "Reordered active items for '$menuKey'"))
    }

    private fun deleteCascade(item: MenuEntity, deleted: MutableList<String>) {
        val children = menuRepo.findByMenuKeyOrderByPositionAsc(item.targetKey)
        children.forEach { child ->
            deleteCascade(child, deleted)
        }

        if (children.isNotEmpty()) {
            menuRepo.deleteAll(children)
            deleted.add(item.targetKey)
        }

        menuRepo.deleteById(item.id)
        deleted.add("${item.menuKey}[${item.labelRw}]")
    }

    private fun reorder(menuKey: String) {
        val active = menuRepo.findByMenuKeyAndActiveTrueOrderByPositionAsc(menuKey)
        active.forEachIndexed { index, item ->
            val newPos = index + 1
            if (item.position != newPos) {
                menuRepo.save(item.copy(position = newPos))
            }
        }
    }
}