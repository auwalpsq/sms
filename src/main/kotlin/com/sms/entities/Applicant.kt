package com.sms.entities

import jakarta.persistence.*
import java.time.*

@Entity
@Table(name = "applicants")
@DiscriminatorValue("APPLICANT")
class Applicant(

    @Column(unique = true, nullable = false)
    var applicationNumber: String = "",

    @Enumerated(EnumType.STRING)
    var applicationStatus: ApplicationStatus = ApplicationStatus.PENDING,

    @Enumerated(EnumType.STRING)
    var paymentStatus: PaymentStatus = PaymentStatus.UNPAID,

    var submissionDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    var relationshipToGuardian: RelationshipType = RelationshipType.OTHER,

    val previousSchoolName: String? = null,
    val previousClass: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id")
    var guardian: Guardian? = null

) : Person() {

    enum class ApplicationStatus { PENDING, APPROVED, REJECTED }

    enum class PaymentStatus { UNPAID, PARTIALLY_PAID, PAID }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Applicant) return false
        return applicationNumber == other.applicationNumber
    }

    override fun hashCode(): Int {
        return applicationNumber.hashCode()
    }
}
enum class RelationshipType {
    FATHER, MOTHER, BROTHER, SISTER, UNCLE, AUNT,
    GRANDFATHER, GRANDMOTHER, OTHER
}