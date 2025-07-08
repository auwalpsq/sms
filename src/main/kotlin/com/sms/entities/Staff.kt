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

) : ContactPerson() {
    //constructor() : this(staffId = "")

    fun getYearsOfService(): Int = Period.between(employmentDate, LocalDate.now()).years
}

enum class StaffType {
    TEACHING, NON_TEACHING, ADMIN
}

enum class Qualification {
    SSCE, NCE, OND, BACHELORS, MASTERS, PHD, OTHERS
}