package com.sms.entities

import com.sms.enums.Section
import com.sms.enums.Term
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "school_classes")
data class SchoolClass(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "", // e.g., "Primary 3A", "SS1 Science"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val section: Section = Section.PRIMARY,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    val academicSession: AcademicSession? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    val classTeacher: Staff? = null,

    @OneToMany(mappedBy = "schoolClass", cascade = [CascadeType.ALL], orphanRemoval = true)
    val studentAssignments: MutableSet<StudentClassAssignment> = mutableSetOf()
) {
    fun getStudentCount(): Int = studentAssignments.size
}