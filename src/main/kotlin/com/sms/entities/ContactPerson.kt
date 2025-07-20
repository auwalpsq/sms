package com.sms.entities

import jakarta.persistence.*

@Entity
@Table(name = "contact_details")
@DiscriminatorValue("PERSON")
class ContactPerson(
    @Column(unique = true)
    open var phoneNumber: String? = null,

    @Column(unique = true)
    open var email: String? = null,

    open var address: String? = null,

    open var city: String? = null,

    open var state: String? = null
) : Person()