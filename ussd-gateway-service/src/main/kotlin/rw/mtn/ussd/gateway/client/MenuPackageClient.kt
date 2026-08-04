package rw.mtn.ussd.gateway.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod

data class MenuDto(
    val id: Long? = null,
    val menuKey: String = "",
    val position: Int = 0,
    val labelEn: String = "",
    val labelRw: String = "",
    val targetKey: String = "",
    val active: Boolean = true
)

data class PkgDto(
    val label: String,
    val price: Int,
    val confirmText: String
)

@Component
class MenuPackageClient(
    @Value("\${services.admin.url:http://localhost:8083}") private val adminUrl: String,
    private val restTemplate: RestTemplate = RestTemplate()
) {

    fun getMenuItems(menuKey: String): List<MenuDto> {
        return try {
            val response = restTemplate.exchange(
                "$adminUrl/api/menu-package/menus/$menuKey",
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<MenuDto>>() {}
            )
            response.body ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPackages(menuKey: String): List<PkgDto> {
        return try {
            val response = restTemplate.exchange(
                "$adminUrl/api/menu-package/packages/$menuKey",
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<PkgDto>>() {}
            )
            response.body ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
