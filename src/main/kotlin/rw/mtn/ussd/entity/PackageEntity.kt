package rw.mtn.ussd.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "packages")
data class PackageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val menuKey: String = "",

    @Column(nullable = false)
    val sortOrder: Int = 0,

    @Column(nullable = false, length = 500)
    val label: String = "",

    @Column(nullable = false)
    val price: Int = 0,

    @Column(nullable = false, length = 500)
    val confirmText: String = "",

    @Column(nullable = false)
    val active: Boolean = true
)
