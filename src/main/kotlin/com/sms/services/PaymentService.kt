package com.sms.services

import com.sms.entities.Payment
import com.sms.entities.PaymentType
import com.sms.enums.PaymentCategory
import com.sms.enums.PaymentStatus
import com.sms.enums.Term
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

    suspend fun updateStatus(reference: String, status: PaymentStatus) = withContext(Dispatchers.IO) {
        paymentMapper.updateStatus(reference, status)
    }
    suspend fun findAll(): List<Payment> = withContext(Dispatchers.IO) {
        paymentMapper.findAll()
    }
    suspend fun findByApplicantAndTypeAndSessionAndTerm(
        applicantId: Long,
        paymentTypeId: Long,
        sessionId: Long,
        term: Term
    ): Payment? = withContext(Dispatchers.IO) {
        paymentMapper.findByApplicantAndTypeAndSessionAndTerm(applicantId, paymentTypeId, sessionId, term)
    }

    suspend fun findByStatus(status: PaymentStatus): List<Payment> = withContext(Dispatchers.IO) {
        paymentMapper.findByStatus(status)
    }
    suspend fun findByApplicantId(applicantId: Long): List<Payment> = withContext(Dispatchers.IO) {
        paymentMapper.findByApplicantId(applicantId)
    }
    suspend fun findByApplicantIdAndPaymentType(
        applicantId: Long,
        paymentTypeId: Long
    ): Payment? = withContext(Dispatchers.IO) {
        paymentMapper.findByApplicantIdAndPaymentType(applicantId, paymentTypeId)
    }
    suspend fun findLatestByApplicant(applicantId: Long): Payment? = withContext(Dispatchers.IO) {
        paymentMapper.findLatestByApplicant(applicantId)
    }
}