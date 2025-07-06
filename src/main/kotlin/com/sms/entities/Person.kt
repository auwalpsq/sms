package com.sms.entities

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "persons")
@DiscriminatorColumn(name = "person_type")
open class Person(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(name = "person_type", insertable = false, updatable = false)
    open val personType: String = "PERSON",

    open val firstName: String = "",
    open val middleName: String? = null,
    open val lastName: String = "",

    @Enumerated(EnumType.STRING)
    open val gender: Gender = Gender.UNSPECIFIED,

    open val dateOfBirth: LocalDate = LocalDate.now(),

    @Column(unique = true)
    open val phoneNumber: String = "",

    open val email: String? = null,

    open val address: String = "",
    open val city: String = "",
    open val state: String = "",

    @Lob
    open val photo: ByteArray? = null,

    open val createdAt: LocalDateTime = LocalDateTime.now(),
    open val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    open fun getFullName(): String = "$lastName ${middleName?.let { "$it " } ?: ""}$firstName"

    protected constructor() : this(personType = "PERSON")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Person) return false
        return id == other.id && id != 0L
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else super.hashCode()
}

enum class Gender { MALE, FEMALE, UNSPECIFIED }