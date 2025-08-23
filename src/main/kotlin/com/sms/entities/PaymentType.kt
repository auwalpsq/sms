package com.sms.entities

import com.sms.enums.PaymentCategory
import jakarta.persistence.*

@Entity
@Table(name = "payment_types")
data class PaymentType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    var category: PaymentCategory? = null,  // restricts to enum values

    @Column(nullable = false)
    var amount: Double = 0.0, // fixed amount for this type

    var description: String? = null // optional
)