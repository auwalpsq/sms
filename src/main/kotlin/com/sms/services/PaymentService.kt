package com.sms.services

import com.sms.entities.Payment
import com.sms.mappers.PaymentMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class PaymentService(private val paymentMapper: PaymentMapper) {

    suspend fun save(payment: Payment) = withContext(Dispatchers.IO) {
        paymentMapper.save(payment)
    }

    suspend fun findByReference(reference: String): Payment? = withContext(Dispatchers.IO) {
        paymentMapper.findByReference(reference)
    }

    suspend fun updateStatus(reference: String, status: Payment.PaymentStatus) = withContext(Dispatchers.IO) {
        paymentMapper.updateStatus(reference, status)
    }
}