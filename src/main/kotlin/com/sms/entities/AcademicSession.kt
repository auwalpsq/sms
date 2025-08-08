package com.sms.entities

import com.sms.enums.Term
import jakarta.persistence.*
import java.time.Year

@Entity
@Table(name = "academic_sessions")
data class AcademicSession(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var startYear: Int = Year.now().value,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var term: Term = Term.FIRST,

    @Column(nullable = false)
    var isCurrent: Boolean = false
){
    val displaySession: String
        get() = "$startYear/${startYear + 1}"
}