package bt.eti.arthurgregorio

import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import kotlin.reflect.KClass

@Component
class EnhancedHttpClient(
    private val restTemplate: RestTemplate,
    private val retryTemplate: RetryTemplate
) {

    fun <T : Any> exchangeWithRetry(requestEntity: RequestEntity<*>, responseType: KClass<T>): ResponseEntity<T> {
        return retryTemplate.execute<ResponseEntity<T>, RestClientException> {
            restTemplate.exchange(
                requestEntity.url.toString(),
                requestEntity.method!!,
                requestEntity,
                responseType.java
            )
        }
    }
}