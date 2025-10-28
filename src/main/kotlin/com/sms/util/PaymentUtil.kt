package com.sms.util

import com.sms.broadcast.UiBroadcaster
import com.sms.entities.Applicant
import com.sms.services.PaymentVerificationService
import com.sms.services.PaystackService
import com.sms.ui.common.showError
import com.sms.ui.common.showSuccess
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI

object PaymentUiUtil {

    fun startPaystackTransaction(
        ui: UI,
        applicant: Applicant,
        reference: String,
        amount: Double,
        serverCallbackTarget: Component
    ) {
        val safeMiddleName = applicant.middleName?.takeIf { it.isNotBlank() } ?: ""
        ui.page.executeJs(
            """
            const paystack = new PaystackPop();
            paystack.newTransaction({
                key: 'pk_test_1c957236071be45d53fe766576b4f60aaaa0534c',
                email: '${applicant.guardian?.email}',
                amount: ${amount} * 100,
                firstName: '${applicant.firstName}',
                lastName: '${applicant.lastName} $safeMiddleName',
                currency: 'NGN',
                reference: '$reference',
                callback: function(response) {
                    if (response.status === 'success') {
                        ${'$'}0.${'$'}server.paymentSuccess(response.reference);
                    } else {
                        ${'$'}0.${'$'}server.paymentFailed(response.reference);
                    }
                },
                onClose: () => { 
                    $0.${'$'}server.paymentCancelled('$reference');
                }
            });
            """.trimIndent(),
            serverCallbackTarget
        )
    }

    /**
     * Handles the client callback after payment succeeds in Paystack.
     */
    suspend fun handlePaymentSuccess(
        reference: String,
        paystackService: PaystackService,
        paymentVerificationService: PaymentVerificationService,
        ui: UI
    ) {
        if (reference.isBlank()) {
            ui.access { showError("❌ Invalid payment reference.") }
            return
        }

        val verified = try {
            val ok = paystackService.verify(reference)
            if (ok) {
                paymentVerificationService.verifyAndUpdateApplicant(reference)
                UiBroadcaster.broadcast(
                    "PAYMENT_SUCCESS",
                    mapOf(
                        "reference" to reference,
                        "message" to "A new payment has been successfully verified.",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
            ok
        } catch (e: Exception) {
            false
        }

        ui.access {
            if (verified) {
                showSuccess("✅ Payment verified and recorded. Ref: $reference")
            } else {
                showError("❌ Payment verification failed. Please try again. Ref: $reference")
            }
        }
    }
    suspend fun handlePaymentCancelled(reference: String, ui: UI) {
        ui.access {
            showError("❌ Payment process was cancelled or closed by the user. Ref: $reference")
        }
    }
    suspend fun handlePaymentFailed(reference: String, ui: UI) {
        ui.access {
            showError("❌ Payment failed. Ref: $reference")
        }
    }
}