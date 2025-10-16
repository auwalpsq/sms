package com.sms.entities

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "class_subject_teacher",
    uniqueConstraints = [
        UniqueConstraint(
            columnNames = ["class_id", "subject_id", "academic_session_id"]
        )
    ]
)
data class ClassSubjectTeacher(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    val schoolClass: SchoolClass,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    val subject: Subject,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    val teacher: Staff,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_session_id", nullable = false)
    val academicSession: AcademicSession,

    /** Date when this assignment was created */
    val assignmentDate: LocalDate = LocalDate.now(),

    /** Optional: If the teacher was replaced or assignment ended */
    val endDate: LocalDate? = null
)