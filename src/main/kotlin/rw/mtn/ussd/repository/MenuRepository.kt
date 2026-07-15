package rw.mtn.ussd.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rw.mtn.ussd.entity.MenuEntity

@Repository
interface MenuRepository : JpaRepository<MenuEntity, Long> {

    fun findByMenuKeyAndActiveTrueOrderByPositionAsc(menuKey: String): List<MenuEntity>
    fun findByMenuKeyOrderByPositionAsc(menuKey: String): List<MenuEntity>
}
