package com.sms.entities

import jakarta.persistence.*
import java.time.*

@Entity
@Table(name = "students")
@DiscriminatorValue("STUDENT")
data class Student(
    @Column(unique = true)
    val admissionNumber: String = "",

    val admissionDate: LocalDate = LocalDate.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    val currentClass: SchoolClass = SchoolClass(),

    @Enumerated(EnumType.STRING)
    val schoolLevel: SchoolLevel = SchoolLevel.PRIMARY,

    val bloodGroup: String? = null,
    val genotype: String? = null,
    val knownAllergies: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    val primaryGuardian: Guardian = Guardian(),

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
    id, "STUDENT", firstName, middleName, lastName, gender, dateOfBirth,
    phoneNumber, email, address, city, state, photo, createdAt, updatedAt
) {
    constructor() : this(admissionNumber = "")

    fun getCurrentAge(): Int = Period.between(dateOfBirth, LocalDate.now()).years
}