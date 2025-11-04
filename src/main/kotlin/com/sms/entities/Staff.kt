package com.sms.entities

import com.sms.enums.Qualification
import com.sms.enums.StaffType
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

    val specialization: String? = null, // e.g. "Mathematics", "English", "ICT"

    val employmentDate: LocalDate = LocalDate.now(),

    val yearsOfExperience: Int? = null, // optional extra for HR reporting

) : ContactPerson() {

    fun getYearsOfService(): Int = Period.between(employmentDate, LocalDate.now()).years
}
