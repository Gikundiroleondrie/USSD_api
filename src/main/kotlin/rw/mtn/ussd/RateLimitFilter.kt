package rw.mtn.ussd

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap


@Component
class RateLimitFilter : OncePerRequestFilter() {

    private val maxRequestsPerMinute = 30
    private val hits = ConcurrentHashMap<String, MutableList<Instant>>()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI != "/ussd"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val phone = request.getParameter("phoneNumber") ?: request.remoteAddr
        val now = Instant.now()
        val windowStart = now.minusSeconds(60)

        val recent = hits.computeIfAbsent(phone) { mutableListOf() }
        synchronized(recent) {
            recent.removeIf { it.isBefore(windowStart) }
            if (recent.size >= maxRequestsPerMinute) {
                response.status = 429  
                response.writer.write("END Ongera ugerageze nyuma gato.")
                return
            }
            recent.add(now)
        }

        filterChain.doFilter(request, response)
    }
}
