package com.sms.entities

import jakarta.persistence.*
import java.time.*

@Entity
@Table(name = "staff")
@DiscriminatorValue("STAFF")
data class Staff(
    @Column(unique = true)
    val staffId: String = "",

    @Enumerated(EnumType.STRING)
    val staffType: StaffType = StaffType.TEACHING,

    @Enumerated(EnumType.STRING)
    val qualification: Qualification = Qualification.BACHELORS,

    val employmentDate: LocalDate = LocalDate.now(),

    @ElementCollection
    @CollectionTable(name = "staff_subjects", joinColumns = [JoinColumn(name = "staff_id")])
    val subjects: Set<String> = emptySet(),

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
    id, "STAFF", firstName, middleName, lastName, gender, dateOfBirth,
    phoneNumber, email, address, city, state, photo, createdAt, updatedAt
) {
    constructor() : this(staffId = "")

    fun getYearsOfService(): Int = Period.between(employmentDate, LocalDate.now()).years
}

enum class StaffType {
    TEACHING, NON_TEACHING, ADMIN
}

enum class Qualification {
    SSCE, NCE, OND, BACHELORS, MASTERS, PHD, OTHERS
}