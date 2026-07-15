package rw.mtn.ussd

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class UssdSecurityFilter(
    @Value("\${ussd.api-key:}") private val expectedApiKey: String,
    @Value("\${ussd.allowed-ips:}") private val allowedIpsRaw: String,
    @Value("\${ussd.trusted-proxies:}") private val trustedProxiesRaw: String
) : OncePerRequestFilter() {

    private val allowedIps: Set<String> by lazy {
        allowedIpsRaw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    private val trustedProxies: Set<String> by lazy {
        trustedProxiesRaw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI != "/ussd"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val providedKey = request.getHeader("X-Ussd-Api-Key")

        if (expectedApiKey.isNotBlank()) {
            if (providedKey != expectedApiKey) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.writer.write("END Unauthorized")
                return
            }
        }

        if (allowedIps.isNotEmpty()) {
            val callerIp = clientIp(request)

            if (callerIp !in allowedIps) {
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.writer.write("END Forbidden")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun clientIp(request: HttpServletRequest): String {

        if (trustedProxies.isNotEmpty() && request.remoteAddr in trustedProxies) {
            request.getHeader("X-Forwarded-For")
                ?.split(",")
                ?.firstOrNull()
                ?.trim()
                ?.let {
                    return it
                }
        }

        return request.remoteAddr
    }
}