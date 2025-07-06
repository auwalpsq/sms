package com.sms.entities

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
    val level: SchoolLevel = SchoolLevel.PRIMARY,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    val classTeacher: Staff? = null,

    @OneToMany(mappedBy = "currentClass")
    val students: MutableSet<Student> = mutableSetOf(),

    @ElementCollection
    @CollectionTable(name = "class_subjects", joinColumns = [JoinColumn(name = "class_id")])
    @Column(name = "subject")
    val subjects: MutableSet<String> = mutableSetOf(),

    @Column(nullable = false)
    val academicYear: String = LocalDate.now().year.toString(),

    @Column(nullable = false)
    val term: Term = Term.FIRST
) {
    fun getStudentCount(): Int = students.size
}

enum class SchoolLevel {
    NURSERY,
    PRIMARY,
    SECONDARY_JUNIOR,
    SECONDARY_SENIOR
}

enum class Term {
    FIRST, SECOND, THIRD
}