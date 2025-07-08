package com.sms.entities

import jakarta.persistence.*

@Entity
@Table(name = "contact_details")
@DiscriminatorValue("PERSON")
class ContactPerson(
    @Column(unique = true, nullable = false)
    val phoneNumber: String? = null,

    @Column(unique = true)
    val email: String? = null,

    @Column(nullable = false)
    val address: String? = null,

    @Column(nullable = false)
    val city: String? = null,

    @Column(nullable = false)
    val state: String? = null
) : Person()