package com.sms.ui.admin.components

import com.sms.entities.PaymentType
import com.sms.enums.PaymentCategory
import com.sms.ui.common.BaseFormDialog
import com.sms.util.FormatUtil
import com.sms.util.launchUiCoroutine
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextField

class PaymentTypeFormDialog(
    onSave: suspend (PaymentType) -> Unit,
    onDelete: suspend (PaymentType) -> Unit,
    onChange: () -> Unit,
) : BaseFormDialog<PaymentType>(
    dialogTitle = "Payment Type",
    onSave = onSave,
    onDelete = onDelete,
    onChange = onChange
) {

    private val category = ComboBox<PaymentCategory>("Category").apply {
        setItems(*PaymentCategory.values())
        isRequired = true
    }

    private val amount = NumberField("Amount").apply {
        isRequired = true
        min = 0.0
        step = 0.01
        placeholder = "Enter amount"

        addValueChangeListener { e ->
            if (e.value != null) {
                // Format the amount using FormatUtil
                val formatted = FormatUtil.formatCurrency(e.value!!)
                this.helperText = formatted // show formatted under the field
            } else {
                this.helperText = ""
            }
        }
    }

    private val description = TextField("Description").apply {
        placeholder = "Optional"
    }

    override fun buildForm(formLayout: FormLayout) {
        formLayout.add(category, amount, description)
        formLayout.responsiveSteps = listOf(
            FormLayout.ResponsiveStep("0", 1)
        )
    }

    override fun configureBinder() {
        binder.forField(category)
            .asRequired("Category is required")
            .bind(PaymentType::category, PaymentType::category::set)

        binder.forField(amount)
            .asRequired("Amount is required")
            .bind(PaymentType::amount, PaymentType::amount::set)

        binder.forField(description)
            .bind(PaymentType::description, PaymentType::description::set)
    }

    override fun createNewInstance(): PaymentType = PaymentType()

    override fun getEntityType(): Class<PaymentType> = PaymentType::class.java
}