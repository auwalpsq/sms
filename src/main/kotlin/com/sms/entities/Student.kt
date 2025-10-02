package com.sms.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "students",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["applicant_id", "admitted_session_id"])
    ]
)
data class Student(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // 🔹 Unique student admission number
    @Column(unique = true, nullable = false)
    val admissionNumber: String,

    // 🔹 Original applicant record
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false, unique = true)
    val applicant: Applicant,

    // 🔹 Academic session of admission
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admitted_session_id", nullable = false)
    val admittedSession: AcademicSession,

    // 🔹 The class student was first admitted into (only one)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admitted_class_id", nullable = false, unique = true)
    val admittedClass: SchoolClass,

    // 🔹 Date of admission
    @CreationTimestamp
    val admittedOn: LocalDate? = LocalDate.now(),

    var admissionAccepted: Boolean = false,
    var admissionAcceptedOn: LocalDateTime? = null
)