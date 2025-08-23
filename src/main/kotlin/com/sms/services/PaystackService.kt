package com.sms.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PaystackService(
    @Value("\${paystack.secret-key}") private val secretKey: String
) {
    private val client = WebClient.builder()
        .baseUrl("https://api.paystack.co")
        .defaultHeader("Authorization", "Bearer $secretKey")
        .build()

    suspend fun verify(reference: String): Boolean = withContext(Dispatchers.IO) {
        val response = client.get()
            .uri("/transaction/verify/{reference}", reference)
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()

        val data = response?.get("data") as? Map<*, *>
        val status = data?.get("status") as? String
        status == "success"
    }
}