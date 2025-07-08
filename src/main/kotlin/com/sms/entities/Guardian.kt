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

    val alternatePhone: String? = null
) : ContactPerson() {
    enum class RelationshipType {
        FATHER, MOTHER, BROTHER, SISTER, UNCLE, AUNT,
        GRANDFATHER, GRANDMOTHER, OTHER
    }
}