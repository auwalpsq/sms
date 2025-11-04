package com.sms.entities

import com.sms.enums.Gender
import jakarta.persistence.*
import java.time.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "persons")
@DiscriminatorColumn(name = "person_type", discriminatorType = DiscriminatorType.STRING)
class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false)
    open var firstName: String? = "",

    open var middleName: String? = null,

    @Column(nullable = false)
    open var lastName: String? = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    open var gender: Gender = Gender.UNSPECIFIED,

    open var dateOfBirth: LocalDate? = null,

    @CreationTimestamp
    @Column(updatable = true)
    open val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    open val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    open fun getFullName(): String = "$lastName ${middleName?.let { "$it " } ?: ""}$firstName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Person) return false
        return id == other.id && id != 0L
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else super.hashCode()
}