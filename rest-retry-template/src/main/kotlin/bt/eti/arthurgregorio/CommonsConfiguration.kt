package bt.eti.arthurgregorio

import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.listener.RetryListenerSupport
import org.springframework.retry.support.RetryTemplate
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

@EnableRetry
@Configuration
class CommonsConfiguration {

    @Bean
    fun retryTemplate(): RetryTemplate {
        return RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(50, 2.0, 3000, true)
            .retryOn(HttpServerErrorException::class.java)
            .withListener(AttemptLoggerListener())
            .build()
    }

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.build()
    }

    inner class AttemptLoggerListener : RetryListenerSupport() {

        private val logger = LoggerFactory.getLogger(AttemptLoggerListener::class.java)

        override fun <T : Any?, E : Throwable?> onError(
            context: RetryContext?,
            callback: RetryCallback<T, E>?,
            throwable: Throwable?
        ) {
            logger.error("Http call failed, retrying... Retry count [{}]", context?.retryCount)
        }
    }
}