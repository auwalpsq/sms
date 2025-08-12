package com.sms.entities

import com.sms.entities.Guardian
import com.sms.entities.Person
import com.sms.entities.SchoolClass
import com.sms.enums.Section
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.Period

@Entity
@Table(name = "students")
@DiscriminatorValue("STUDENT")
class Student(

    @Column(unique = true)
    val admissionNumber: String? = null,

    @Column(nullable = false)
    val admissionDate: LocalDate = LocalDate.now(),

    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL], orphanRemoval = true)
    val classAssignments: MutableSet<StudentClassAssignment> = mutableSetOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val section: Section = Section.PRIMARY,

    val bloodGroup: String? = null,
    val genotype: String? = null,
    val knownAllergies: String? = null,

    @Lob
    val photo: ByteArray? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    val guardian: Guardian? = null

) : Person() {

    val currentAge: Int
        get() = Period.between(dateOfBirth, LocalDate.now()).years
}