package com.sms.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class Payment(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var reference: String = "",

    var amount: Double = 0.0,

    var status: PaymentStatus = PaymentStatus.PENDING,

    var paymentDate: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    var applicant: Applicant? = null

) {
    enum class PaymentStatus {
        PENDING, SUCCESS, FAILED
    }
}