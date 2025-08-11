package com.sms.entities

import com.sms.enums.Section
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

    var previousSchoolName: String? = null,
    var previousClass: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id")
    var guardian: Guardian? = null,

    @Column(name = "application_section")
    @Enumerated(EnumType.STRING)
    var applicationSection: Section =   Section.NURSERY,

    @JoinColumn(name = "intended_class")
    var intendedClass: String? = ""

) : Person() {
    val currentAge: String
        get() = "${Period.between(dateOfBirth, LocalDate.now()).years} year(s) ${Period.between(dateOfBirth, LocalDate.now()).months % 12} month(s)"

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