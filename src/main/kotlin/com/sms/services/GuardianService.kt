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
}