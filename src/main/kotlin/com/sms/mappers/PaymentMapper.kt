package com.sms.mappers

import com.sms.entities.Payment
import com.sms.enums.PaymentCategory
import com.sms.enums.PaymentStatus
import com.sms.enums.Term
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface PaymentMapper {

    fun save(payment: Payment)

    fun findByReference(@Param("reference") reference: String): Payment?

    fun updateStatus(
        @Param("reference") reference: String,
        @Param("status") status: PaymentStatus
    )

    fun findAll(): List<Payment>

    fun findByStatus(@Param("status") status: PaymentStatus): List<Payment>

    fun findByApplicantAndTypeAndSessionAndTerm(
        @Param("applicantId") applicantId: Long,
        @Param("paymentTypeId") paymentTypeId: Long,
        @Param("sessionId") sessionId: Long,
        @Param("term") term: Term
    ): Payment?

    fun findByApplicantId(@Param("applicantId") applicantId: Long): List<Payment>

    fun findByApplicantIdAndPaymentType(
        @Param("applicantId") applicantId: Long,
        @Param("paymentTypeId") paymentTypeId: Long
    ): Payment?

    fun findLatestByApplicant(applicantId: Long): Payment?
}