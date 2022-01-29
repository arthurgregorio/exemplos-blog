package bt.eti.arthurgregorio

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.ExpectedCount.once
import org.springframework.test.web.client.ExpectedCount.times
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

@SpringBootTest
class ApplicationTests {

    @Value("\${cat-service.url}")
    private lateinit var catServiceUrl: String

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var catService: CatService

    private lateinit var mockServer: MockRestServiceServer

    @BeforeEach
    fun setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate)
    }

    @Test
    fun `should find a cat fact`() {

        mockServer.expect(once(), requestTo(catServiceUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Some random cat fact")
            )

        val catFact = catService.findCatFact()
        assertThat(catFact).isEqualTo("Some random cat fact")

        mockServer.verify()
    }

    @Test
    fun `should retry three times before throw error`() {

        mockServer.expect(times(3), requestTo(catServiceUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR))

        assertThrows<HttpServerErrorException> { catService.findCatFact() }

        mockServer.verify()
    }

    @Test
    fun `should not retry when client error occur`() {

        mockServer.expect(once(), requestTo(catServiceUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST))

        assertThrows<HttpClientErrorException> { catService.findCatFact() }

        mockServer.verify()
    }
}
