package rw.mtn.ussd.admin.domain

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "menus")
data class MenuEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false) val menuKey: String = "",
    @Column(nullable = false) val position: Int = 0,
    @Column(nullable = false) val labelRw: String = "",
    @Column(nullable = false) val labelEn: String = "",
    @Column(nullable = false) val targetKey: String = "",
    @Column(nullable = false) val requiresPhone: Boolean = false,
    @Column(nullable = false) val active: Boolean = true
)

@Entity
@Table(name = "packages")
data class PackageEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false) val menuKey: String = "",
    @Column(nullable = false) val label: String = "",
    @Column(nullable = false) val price: Int = 0,
    @Column(nullable = false) val confirmText: String = "",
    @Column(nullable = false) val sortOrder: Int = 0,
    @Column(nullable = false) val active: Boolean = true
)

interface MenuRepository : JpaRepository<MenuEntity, Long> {
    fun findByMenuKeyAndActiveTrueOrderByPositionAsc(menuKey: String): List<MenuEntity>
    fun findByMenuKeyOrderByPositionAsc(menuKey: String): List<MenuEntity>
}

interface PackageRepository : JpaRepository<PackageEntity, Long> {
    fun findByMenuKeyAndActiveTrueOrderBySortOrderAsc(menuKey: String): List<PackageEntity>
    fun findByMenuKeyOrderBySortOrderAsc(menuKey: String): List<PackageEntity>
}
