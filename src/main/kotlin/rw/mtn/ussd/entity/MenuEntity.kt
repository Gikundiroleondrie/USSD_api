package rw.mtn.ussd.entity

import jakarta.persistence.*

@Entity
@Table(name = "menus")
data class MenuEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val menuKey: String = "",

    @Column(nullable = false)
    val position: Int = 0,

    @Column(nullable = false)
    val labelRw: String = "",

    @Column(nullable = false)
    val labelEn: String = "",

    @Column(nullable = false)
    val targetKey: String = "",

    @Column(nullable = false)
    val requiresPhone: Boolean = false,

    @Column(nullable = false)
    val active: Boolean = true
)