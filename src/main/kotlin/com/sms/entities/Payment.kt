package com.sms.entities

import com.sms.enums.PaymentStatus
import com.sms.enums.Term
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class Payment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    var applicant: Applicant? = null,  // Payment is usually linked to applicant

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    var guardian: Guardian? = null, // Optional, in case guardian pays directly

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_type_id", nullable = false)
    var paymentType: PaymentType? = null, // Which fee is being paid

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_session_id", nullable = false)
    var academicSession: AcademicSession? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var term: Term? = null, // e.g. FIRST, SECOND, THIRD

    @Column(nullable = false, unique = true)
    var reference: String, // Paystack reference

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)