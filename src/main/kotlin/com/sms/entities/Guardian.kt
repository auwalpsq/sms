package com.sms.entities

import jakarta.persistence.*
import java.time.*

@Entity
@Table(name = "guardians")
@DiscriminatorValue("GUARDIAN")
data class Guardian(
    @Column(unique = true)
    var guardianId: String? = null,

    var occupation: String? = null,
    var employer: String? = null,

    var alternatePhone: String? = null
) : ContactPerson()