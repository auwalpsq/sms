package com.sms.entities

import jakarta.persistence.*

@Entity
@Table(name = "school_profile")
open class SchoolProfile(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) open val id: Long = 0,
        @Column(nullable = false) open var name: String = "School Management System",
        open var address: String? = null,
        open var email: String? = null,
        open var phoneNumber: String? = null,
        open var motto: String? = null,
        open var logoUrl: String? = null,
        open var website: String? = null,
        @OneToOne
        @JoinColumn(name = "current_session_id")
        open var currentSession: AcademicSession? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SchoolProfile) return false
        return id == other.id && id != 0L
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else super.hashCode()
}
