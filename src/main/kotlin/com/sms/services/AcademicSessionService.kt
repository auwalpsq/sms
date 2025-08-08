package com.sms.services

import com.sms.entities.AcademicSession
import com.sms.mappers.AcademicSessionMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AcademicSessionService(
    private val academicSessionMapper: AcademicSessionMapper
) {

    @Transactional
    suspend fun findAll(): List<AcademicSession>{
        return academicSessionMapper.findAll()
    }

    suspend fun findById(id: Long): AcademicSession = academicSessionMapper.findById(id)

    suspend fun findCurrent(): AcademicSession? = academicSessionMapper.findCurrentSession()

    suspend fun save(session: AcademicSession) {
        // Rule 1: Unique year + term
        val existing = academicSessionMapper.findByYearAndTerm(session.startYear, session.term)
        if (existing != null && existing.id != session.id) {
            throw IllegalArgumentException("Academic session for year ${session.startYear} and term ${session.term} already exists.")
        }

        // Rule 2: Only one session can be current
        if (session.isCurrent) {
            val current = academicSessionMapper.findCurrentSession()
            if (current != null && current.id != session.id) {
                current.isCurrent = false
                academicSessionMapper.update(current)
            }
        }

        if (session.id == 0L) {
            academicSessionMapper.save(session)
        } else {
            academicSessionMapper.update(session)
        }
    }

    suspend fun deleteById(id: Long) {
        academicSessionMapper.deleteById(id)
    }
}