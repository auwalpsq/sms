package com.sms.entities

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "applicants")
@DiscriminatorValue("APPLICANT")
data class Applicant(
    @Column(unique = true)
    val applicationNumber: String = "",

    @Enumerated(EnumType.STRING)
    val applicationLevel: SchoolLevel = SchoolLevel.PRIMARY,

    val applicationDate: LocalDate = LocalDate.now(),

    @Enumerated(EnumType.STRING)
    val applicationStatus: ApplicationStatus = ApplicationStatus.PENDING,

    val previousSchoolName: String? = null,
    val previousClass: String? = null,

    val parentName: String = "",
    val parentPhone: String = "",
    val parentEmail: String? = null,
    val parentRelationship: String = "",

    override val id: Long = 0,
    override val firstName: String = "",
    override val middleName: String? = null,
    override val lastName: String = "",
    override val gender: Gender = Gender.UNSPECIFIED,
    override val dateOfBirth: LocalDate = LocalDate.now(),
    override val phoneNumber: String = "",
    override val email: String? = null,
    override val address: String = "",
    override val city: String = "",
    override val state: String = "",
    override val photo: ByteArray? = null,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : Person(
    id, "APPLICANT", firstName, middleName, lastName, gender, dateOfBirth,
    phoneNumber, email, address, city, state, photo, createdAt, updatedAt
) {
    enum class ApplicationStatus { PENDING, APPROVED, REJECTED, ADMITTED }

    constructor() : this(applicationNumber = "")
}