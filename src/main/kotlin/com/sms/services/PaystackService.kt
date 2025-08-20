package com.sms.services

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class PaystackService(private val restTemplate: RestTemplate) {

    private val secretKey = "sk_test_e7fd3716c1beec1ca1c788208040db93d472ec93" // From Paystack dashboard

    fun verifyPayment(reference: String): Boolean {
        val url = "https://api.paystack.co/transaction/verify/$reference"
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $secretKey")
        }
        val entity = HttpEntity<String>(headers)
        val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)

        return response.statusCode.is2xxSuccessful &&
                response.body?.contains("\"status\":true") == true
    }
}