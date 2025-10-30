package com.sms.services

import com.sms.entities.Guardian
import com.sms.mappers.GuardianMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class GuardianService(
    private val guardianMapper: GuardianMapper,
    private val userDetailsManager: MyUserDetailsManager
) {
    suspend fun save(guardian: Guardian): Guardian = withContext(Dispatchers.IO) {
        if (guardian.id == 0L) {
            guardianMapper.insertIntoPersons(guardian)
            guardianMapper.insertIntoContactDetails(guardian)
            guardian.guardianId = generateGuardianId(guardian.id)
            guardianMapper.insertIntoGuardians(guardian)

            userDetailsManager.createUserWithRoles(
                username = guardian.email,
                password = guardian.phoneNumber,
                email = guardian.email,
                roleNames = setOf("ROLE_GUARDIAN"),
                enabled = true,
                guardian
            )
            guardian
        } else {
            guardianMapper.update(guardian)

            guardian
        }

    }

    suspend fun delete(guardian: Guardian): Int = withContext(Dispatchers.IO) {
        guardian.id?.let { guardianMapper.delete(it) } ?: 0
    }

    suspend fun findById(id: Long?): Guardian? = withContext(Dispatchers.IO) {
        guardianMapper.findById(id)
    }

    suspend fun findAll(): List<Guardian> = withContext(Dispatchers.IO) {
        guardianMapper.findAll()
    }

    suspend fun existsByEmail(email: String): Boolean = withContext(Dispatchers.IO) {
        guardianMapper.existsByEmail(email)
    }
    fun generateGuardianId(latestId: Long): String {
        val year = LocalDate.now().year
        return "GDN-$year-${String.format("%04d", latestId)}"
    }
    suspend fun findByEmail(email: String): Guardian? = withContext(Dispatchers.IO) {
        guardianMapper.findByEmail(email)
    }
    /**
     * Returns a page of guardians. `page` is 1-based (page = 1 => first page).
     */
    suspend fun findPage(page: Int, pageSize: Int): List<Guardian> = withContext(Dispatchers.IO) {
        val safePage = if (page < 1) 1 else page
        val offset = (safePage - 1) * pageSize
        guardianMapper.findPage(offset, pageSize)
    }

    /**
     * Search with paging. `page` is 1-based.
     */
    suspend fun findPageBySearch(query: String, page: Int, pageSize: Int): List<Guardian> = withContext(Dispatchers.IO) {
        val safePage = if (page < 1) 1 else page
        val offset = (safePage - 1) * pageSize
        guardianMapper.findPageBySearch(query, offset, pageSize)
    }

    /**
     * Simple search without paging (optional).
     */
    suspend fun search(query: String): List<Guardian> = withContext(Dispatchers.IO) {
        guardianMapper.search(query)
    }

    /**
     * Optional count for total pages if you want to show page numbers.
     */
    suspend fun countAll(): Int = withContext(Dispatchers.IO) {
        guardianMapper.countAll()
    }
}