package com.sms.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "student_class_assignments",
    uniqueConstraints = [UniqueConstraint(columnNames = ["student_id", "class_id", "session_id"])]
)
data class StudentClassAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    val student: Student? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    val schoolClass: SchoolClass? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    val academicSession: AcademicSession? = null,

    @Column(nullable = false)
    val assignedDate: LocalDateTime = LocalDateTime.now()
)