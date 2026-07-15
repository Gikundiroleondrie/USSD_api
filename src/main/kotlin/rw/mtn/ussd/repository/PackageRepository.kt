package rw.mtn.ussd.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rw.mtn.ussd.entity.PackageEntity

@Repository
interface PackageRepository : JpaRepository<PackageEntity, Long> {

    fun findByMenuKeyAndActiveTrueOrderBySortOrderAsc(menuKey: String): List<PackageEntity>
    fun findByMenuKeyOrderBySortOrderAsc(menuKey: String): List<PackageEntity>
}