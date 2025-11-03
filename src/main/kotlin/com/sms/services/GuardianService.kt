package com.sms.services

import com.sms.entities.Guardian
import com.sms.mappers.GuardianMapper
import com.sms.util.PageResult
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
            // Insert into base tables
            guardianMapper.insertIntoPersons(guardian)
            guardianMapper.insertIntoContactDetails(guardian)
            guardian.guardianId = generateGuardianId(guardian.id)
            guardianMapper.insertIntoGuardians(guardian)

            // Auto-create user account for guardian
            userDetailsManager.createUserWithRoles(
                username = guardian.email,
                password = guardian.phoneNumber,
                email = guardian.email,
                roleNames = setOf("ROLE_GUARDIAN"),
                enabled = true,
                guardian
            )
        } else {
            guardianMapper.update(guardian)
        }
        guardian
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

    suspend fun findByEmail(email: String): Guardian? = withContext(Dispatchers.IO) {
        guardianMapper.findByEmail(email)
    }

    fun generateGuardianId(latestId: Long): String {
        val year = LocalDate.now().year
        return "GDN-$year-${String.format("%04d", latestId)}"
    }

    /**
     * ✅ Unified paginated finder with optional search query.
     * `page` is 1-based (page = 1 => first page).
     */
    suspend fun findPage(query: String?, page: Int, pageSize: Int): PageResult<Guardian> = withContext(Dispatchers.IO) {
        val safePage = if (page < 1) 1 else page
        val offset = (safePage - 1) * pageSize

        val items = guardianMapper.findPage(query, offset, pageSize)
        val totalCount = guardianMapper.countFiltered(query)

        PageResult(items, totalCount)
    }


    /**
     * ✅ Smarter variant: fetch pageSize + 1 records to check if next page exists.
     * Useful for Vaadin LazyDataProvider.
     */
    suspend fun findPageWithNextCheck(query: String?, page: Int, pageSize: Int): Pair<List<Guardian>, Boolean> =
        withContext(Dispatchers.IO) {
            val safePage = if (page < 1) 1 else page
            val offset = (safePage - 1) * pageSize
            val results = guardianMapper.findPage(query, offset, pageSize + 1)
            val hasNext = results.size > pageSize
            val trimmed = if (hasNext) results.dropLast(1) else results
            Pair(trimmed, hasNext)
        }

    /**
     * ✅ Optional count helper if you need total pages in the UI.
     */
    suspend fun countFiltered(query: String?): Int = withContext(Dispatchers.IO) {
        guardianMapper.countFiltered(query)
    }
}
