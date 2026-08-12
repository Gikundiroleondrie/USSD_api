package rw.mtn.ussd.admin.admin

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import rw.mtn.ussd.admin.domain.*
import java.security.MessageDigest
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil(
    @Value("\${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") private val secret: String,
    @Value("\${admin.jwt-expiration-hours:10}") private val expirationHours: Long
) {
    private val key: SecretKey? by lazy {
        if (secret.isBlank()) null
        else {
            val bytes = secret.toByteArray(Charsets.UTF_8)
            val padded = if (bytes.size >= 32) bytes else bytes.copyOf(32)
            Keys.hmacShaKeyFor(padded)
        }
    }

    fun isConfigured(): Boolean = key != null

    fun generateToken(username: String): String {
        val signingKey = key ?: error("admin.jwt-secret is not configured")
        val now = Date()
        val expiry = Date(now.time + expirationHours * 3_600_000L)
        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    fun validateAndGetSubject(token: String): String? {
        val signingKey = key ?: return null
        return try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (ex: Exception) {
            null
        }
    }
}

data class LoginRequest(val username: String = "", val password: String = "")
data class LoginResponse(val token: String)

@RestController
@RequestMapping("/admin")
class AdminAuthController(
    private val jwtUtil: JwtUtil,
    @Value("\${admin.username:admin}") private val adminUsername: String,
    @Value("\${admin.password:admin123}") private val adminPassword: String
) {
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<Any> {
        val credentialsOk = constantTimeEquals(req.username, adminUsername) && constantTimeEquals(req.password, adminPassword)
        if (!credentialsOk) {
            return ResponseEntity.status(401).body(mapOf("error" to "Invalid username or password"))
        }
        return ResponseEntity.ok(LoginResponse(jwtUtil.generateToken(adminUsername)))
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
}

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
        menuRepo.findById(id).map { ResponseEntity.ok(it) }.orElse(ResponseEntity.notFound().build())

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
        val existing = menuRepo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
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
        children.forEach { child -> deleteCascade(child, deleted) }
        if (children.isNotEmpty()) {
            menuRepo.deleteAll(children)
            deleted.add(item.targetKey)
        }
        menuRepo.deleteById(item.id!!)
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
        return ResponseEntity.ok(mapOf("message" to "Package deleted."))
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
