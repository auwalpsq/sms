package com.sms.entities

import com.sms.enums.Section
import jakarta.persistence.*

@Entity
@Table(
    name = "subject",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["name", "school_section_id"])
    ]
)
data class Subject(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    val code: String? = null, // optional short code, e.g., MTH101

    val description: String? = null,

    @Enumerated(EnumType.STRING)
    val subjectType: SubjectType = SubjectType.CORE,

    @Enumerated(EnumType.STRING)
    val schoolSection: Section = Section.PRIMARY,

    val isActive: Boolean = true
)

enum class SubjectType {
    CORE, ELECTIVE
}