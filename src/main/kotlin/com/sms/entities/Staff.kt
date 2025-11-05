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
    var staffId: String = "",

    @Enumerated(EnumType.STRING)
    var staffType: StaffType = StaffType.TEACHING,

    @Enumerated(EnumType.STRING)
    var qualification: Qualification = Qualification.BACHELORS,

    var specialization: String? = null, // e.g. "Mathematics", "English", "ICT"

    var employmentDate: LocalDate = LocalDate.now(),

    var yearsOfExperience: Int? = null, // optional extra for HR reporting

) : ContactPerson() {

    fun getYearsOfService(): Int = Period.between(employmentDate, LocalDate.now()).years
}
