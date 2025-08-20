package com.sms.ui.components

import com.vaadin.flow.component.ClientCallable
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dependency.JavaScript
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout


@JavaScript("https://js.paystack.co/v1/inline.js")
class PaymentView : VerticalLayout() {
    init {
        val payButton: Button = Button("Pay with Paystack", { e ->
            UI.getCurrent().getPage().executeJs(
                "var handler = PaystackPop.setup({" +
                        "   key: 'pk_test_1c957236071be45d53fe766576b4f60aaaa0534c'," +  // your public key
                        "   email: 'customer@email.com'," +
                        "   amount: 5000 * 100," +  // amount in kobo
                        "   currency: 'NGN'," +
                        "   callback: function(response) { $0.\$server.paymentSuccess(response.reference); }," +
                        "   onClose: function() { alert('Payment window closed'); }" +
                        "}); handler.openIframe();",
                getElement()
            )
        })
        add(payButton)
    }

    @ClientCallable
    private fun paymentSuccess(reference: String?) {
        // Call your backend to verify payment via Paystack REST API
        Notification.show("Payment successful! Ref: " + reference)
    }
}
