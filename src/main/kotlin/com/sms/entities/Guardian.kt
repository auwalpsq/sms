package com.sms.entities

import jakarta.persistence.*
import java.time.*

@Entity
@Table(name = "guardians")
@DiscriminatorValue("GUARDIAN")
data class Guardian(
    @Column(unique = true)
    val guardianId: String = "",

    @Enumerated(EnumType.STRING)
    val relationshipToStudent: RelationshipType = RelationshipType.OTHER,

    val occupation: String? = null,
    val employer: String? = null,

    val alternatePhone: String? = null,

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
    id, "GUARDIAN", firstName, middleName, lastName, gender, dateOfBirth,
    phoneNumber, email, address, city, state, photo, createdAt, updatedAt
) {
    enum class RelationshipType {
        FATHER, MOTHER, BROTHER, SISTER, UNCLE, AUNT,
        GRANDFATHER, GRANDMOTHER, OTHER
    }

    constructor() : this(guardianId = "")
}