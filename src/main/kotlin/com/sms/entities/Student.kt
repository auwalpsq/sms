package com.sms.entities

import jakarta.persistence.*
import java.time.*

@Entity
@Table(name = "students")
@DiscriminatorValue("STUDENT")
class Student(
    @Column(unique = true, nullable = false)
    val admissionNumber: String? = null,

    @Column(nullable = false)
    val admissionDate: LocalDate = LocalDate.now(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id")
    val currentClass: SchoolClass? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val schoolLevel: SchoolLevel = SchoolLevel.PRIMARY,

    val bloodGroup: String? = null,
    val genotype: String? = null,
    val knownAllergies: String? = null,

    @Lob
    val photo: ByteArray? = null,

    @Enumerated(EnumType.STRING)
    var applicationStatus: ApplicationStatus = ApplicationStatus.PENDING,

    val previousSchoolName: String? = null,
    val previousClass: String? = null,

    // ✅ New field: Student linked to Guardian
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id")
    var guardian: Guardian? = null

) : Person() {
    val currentAge: Int
        get() = Period.between(dateOfBirth, LocalDate.now()).years

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Student) return false
        return admissionNumber == other.admissionNumber
    }

    override fun hashCode(): Int = admissionNumber.hashCode()

    enum class ApplicationStatus { PENDING, APPROVED, REJECTED, ADMITTED }
}