package rw.mtn.ussd

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["DB_URL=", "DB_USERNAME=", "DB_PASSWORD="])
class ApplicationStartupTest {

    @Test
    fun contextLoads() {
    }
}
