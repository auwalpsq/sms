package com.sms.services

import com.sms.entities.Staff
import com.sms.enums.StaffType
import com.sms.mappers.PersonMapper
import com.sms.mappers.ContactPersonMapper
import com.sms.mappers.StaffMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StaffService(
    private val personMapper: PersonMapper,
    private val contactPersonMapper: ContactPersonMapper,
    private val staffMapper: StaffMapper,
    private val userDetailsManager: MyUserDetailsManager
) {

    /**
     * Get all staff records with optional search and pagination.
     */
    suspend fun findAll(search: String? = null, page: Int = 0, size: Int = 20): List<Staff> = withContext(Dispatchers.IO) {
        val offset = page * size
        staffMapper.findAll(search, offset, size)
    }

    /**
     * Count total staff records (used for pagination)
     */
    suspend fun countAll(search: String? = null): Int = withContext(Dispatchers.IO) {
        staffMapper.countAll(search)
    }

    /**
     * Find staff by ID (includes person and contact details)
     */
    suspend fun findById(id: Long): Staff? = withContext(Dispatchers.IO) {
        staffMapper.findById(id)
    }

    /**
     * Save new staff record across all related tables.
     */
    @Transactional
    suspend fun save(staff: Staff): Staff = withContext(Dispatchers.IO) {
        if(staff.staffId.isBlank()){
            staff.staffId = generateStaffId(staff)
        }

        // 1️⃣ Insert into persons table
        personMapper.saveWithType(staff, "STAFF")
        // ID is auto-generated

        // 2️⃣ Insert into contact_details table using same ID
        contactPersonMapper.save(staff)

        // 3️⃣ Insert into staff table
        staffMapper.save(staff)

        // Auto-create user account for staff
        userDetailsManager.createUserWithRoles(
            username = staff.email,
            password = staff.phoneNumber,
            email = staff.email,
            roleNames = setOf("ROLE_STAFF"),
            enabled = true,
            person = staff
        )

        staff
    }

    /**
     * Update existing staff information across all related tables.
     */
    @Transactional
    suspend fun update(staff: Staff): Staff = withContext(Dispatchers.IO) {
        personMapper.update(staff)
        contactPersonMapper.update(staff)
        staffMapper.update(staff)
        staff
    }

    /**
     * Delete staff record across all related tables.
     * We delete in reverse order to maintain FK integrity.
     */
    @Transactional
    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        staffMapper.delete(id)
        contactPersonMapper.delete(id)
        personMapper.delete(id)
    }

    private suspend fun generateStaffId(staff: Staff): String {
        val prefix = if (staff.staffType == StaffType.TEACHING) "TCH" else "ADM"
        val timestamp = System.currentTimeMillis().toString().takeLast(5)
        return "$prefix-${timestamp}"
    }

}