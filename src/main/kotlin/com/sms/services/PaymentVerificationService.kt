package com.sms.services

import com.sms.entities.Applicant
import com.sms.entities.Payment
import com.sms.enums.PaymentStatus
import org.springframework.stereotype.Service

@Service
class PaymentVerificationService(
    private val paymentService: PaymentService,
    private val applicantService: ApplicantService
) {
    /**
     * Verifies a payment and updates both the Payment and its linked Applicant.
     * This method is idempotent: calling it multiple times with the same reference
     * will not cause duplicate updates.
     */
    suspend fun verifyAndUpdateApplicant(reference: String) {
        val payment = paymentService.findByReference(reference) ?: return

        // Only update if not already marked successful
        if (payment.status != PaymentStatus.SUCCESS) {
            payment.status = PaymentStatus.SUCCESS
            paymentService.updateStatus(reference, payment.status) // ✅ persist payment update
        }

        // Update applicant if linked
        payment.applicant?.let { applicant ->
            if (applicant.paymentStatus != Applicant.PaymentStatus.PAID) {
                applicant.paymentStatus = Applicant.PaymentStatus.PAID
                applicantService.save(applicant) // ✅ persist applicant update
            }
        }
    }
}