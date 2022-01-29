package bt.eti.arthurgregorio

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class CatService(
    @Value("\${cat-service.url}")
    private val catServiceUrl: String,
    private val enhancedHttpClient: EnhancedHttpClient
) {

    fun findCatFact(): String {

        // O cliente http ainda poderia estar encapsulado em outro componente, isso seria util para termos
        // um maior reaproveitamento do código

        val uri = UriComponentsBuilder.fromHttpUrl(catServiceUrl).build().toUri()
        return enhancedHttpClient.exchangeWithRetry(RequestEntity("", HttpMethod.GET, uri), String::class).body!!
    }
}