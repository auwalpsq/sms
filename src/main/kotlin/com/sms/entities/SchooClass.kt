package com.sms.entities

import com.sms.enums.Grade
import com.sms.enums.Level
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
    var name: String = "", // e.g., "Primary 3A", "SS1 Science"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var level: Level = Level.ONE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var grade: Grade = Grade.A,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var section: Section = Section.PRIMARY,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    var classTeacher: Staff? = null,

    @OneToMany(mappedBy = "schoolClass", cascade = [CascadeType.ALL], orphanRemoval = true)
    var studentAssignments: MutableSet<StudentClassAssignment> = mutableSetOf()
) {
    fun getStudentCount(): Int = studentAssignments.size
}