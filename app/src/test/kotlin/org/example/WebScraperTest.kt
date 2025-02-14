package org.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import kotlin.test.Test

class WebScraperTest {
    private val correctHabrUrl = Url("https://habr.com/ru/")
    private val invalidHabrUrl = Url("https://habr.com/notfound")
    private val title = "Безопасность данных в BI-системе"

    private val clientMock = HttpClient(MockEngine) {
        expectSuccess = false
        engine {
            addHandler { request ->
                when (request.url) {
                    correctHabrUrl -> {
                        respond(
                            "<html><body><h2 class='tm-title'>${title}</h2></body></html>",
                            status = HttpStatusCode.OK
                        )
                    }
                    invalidHabrUrl -> {
                        respond(
                            "Page was not found",
                            status = HttpStatusCode.NotFound
                        )
                    }
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }

    @Test
    fun `receive first title from habr main page`() = runBlocking {
        val scraper = WebScraper(clientMock, correctHabrUrl)
        assertEquals(scraper.getFirstTitleFromHabrMainPage(), title)
    }

    @Test
    fun `try to get page from invalid url`() = runBlocking {
        val scraper = WebScraper(clientMock, invalidHabrUrl)
        assertNull(scraper.getFirstTitleFromHabrMainPage())
    }
}
