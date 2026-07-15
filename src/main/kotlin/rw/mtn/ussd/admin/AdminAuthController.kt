package rw.mtn.ussd.admin

import java.security.MessageDigest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class LoginRequest(val username: String = "", val password: String = "")
data class LoginResponse(val token: String)

@RestController
@RequestMapping("/admin")
class AdminAuthController(
    private val jwtUtil: JwtUtil,
    @Value("\${admin.username:admin}") private val adminUsername: String,
    @Value("\${admin.password:}") private val adminPassword: String
) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<Any> {
        if (adminPassword.isBlank() || !jwtUtil.isConfigured()) {
            return ResponseEntity.status(503)
                .body(mapOf("error" to "Admin login is not configured (ADMIN_PASSWORD / ADMIN_JWT_SECRET missing)"))
        }

        val credentialsOk =
            constantTimeEquals(req.username, adminUsername) && constantTimeEquals(req.password, adminPassword)

        if (!credentialsOk) {
            return ResponseEntity.status(401).body(
                mapOf(
                    "error" to "Invalid username or password",
                    "debug" to mapOf(
                        "expectedUsernameLength" to adminUsername.length,
                        "providedUsernameLength" to req.username.length,
                        "usernameMatch" to (req.username == adminUsername),
                        "expectedPasswordLength" to adminPassword.length,
                        "providedPasswordLength" to req.password.length,
                        "expectedPasswordPrefix" to adminPassword.take(2),
                        "providedPasswordPrefix" to req.password.take(2),
                        "passwordMatch" to (req.password == adminPassword)
                    )
                )
            )
        }

        return ResponseEntity.ok(LoginResponse(jwtUtil.generateToken(adminUsername)))
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
}