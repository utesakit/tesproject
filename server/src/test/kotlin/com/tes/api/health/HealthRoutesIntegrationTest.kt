package com.tes.api.health

import com.tes.domain.health.Health
import com.tes.domain.health.HealthService
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthRoutesTest {

    @Test
    fun `GET health returns UP and non-negative uptime`() = testApplication {
        val serverStartTime = Instant.now().minusSeconds(1)

        application {
            install(ServerContentNegotiation) {
                json()
            }
            routing {
                healthRoutes(
                    healthService = HealthService(),
                    serverStartTime = serverStartTime
                )
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)

        val body: Health = response.body()
        assertEquals("UP", body.status)
        assertTrue(body.uptimeSeconds > 0)
    }
}
