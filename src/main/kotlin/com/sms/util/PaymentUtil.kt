package com.sms.util

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
        val email = applicant.guardian?.email ?: ""
        val amountKobo = amount * 100
        val fullName = "${applicant.lastName} $safeMiddleName"
        val firstName = applicant.firstName

        // Pass parameters as arguments to avoid JS syntax errors with quotes
        ui.page.executeJs(
                """
            const server = $0.${'$'}server;
            const apiKey = $1;
            const email = $2;
            const amount = $3;
            const firstName = $4;
            const lastName = $5;
            const ref = $6;

            console.log("Initializing Paystack transaction...", { email, amount, ref });
            try {
                const paystack = new PaystackPop();
                paystack.newTransaction({
                    key: apiKey,
                    email: email,
                    amount: amount,
                    firstName: firstName,
                    lastName: lastName,
                    currency: 'NGN',
                    reference: ref,
                onSuccess: function(response) {
                        console.log("Paystack onSuccess", response);
                        
                        // Try logging to server if possible
                        if (server.log) server.log("Paystack onSuccess: " + response.reference);

                        if (response.status === 'success') {
                            server.paymentSuccess(response.reference);
                        } else {
                            server.paymentFailed(response.reference);
                        }
                    },
                    onCancel: () => { 
                        console.log("Paystack onCancel");
                         if (server.log) server.log("Paystack onCancel");
                        server.paymentCancelled(ref);
                    }
                });
            } catch(e) {
                console.error("Paystack Init Error", e);
                const msg = "Paystack Error: " + e.message;
                alert(msg);
                if (server.log) server.log(msg);
            }
            """.trimIndent(),
                serverCallbackTarget, // $0
                "pk_test_1c957236071be45d53fe766576b4f60aaaa0534c", // $1
                email, // $2
                amountKobo, // $3
                firstName, // $4
                fullName, // $5
                reference // $6
        )
    }

    /** Handles the client callback after payment succeeds in Paystack. */
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

        val verified =
                try {
                    val ok = paystackService.verify(reference)
                    if (ok) {
                        paymentVerificationService.verifyAndUpdateApplicant(reference)
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
        ui.access { showError("❌ Payment failed. Ref: $reference") }
    }
}
