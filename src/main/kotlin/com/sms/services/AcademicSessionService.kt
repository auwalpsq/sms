package com.sms.services

import com.sms.entities.AcademicSession
import com.sms.mappers.AcademicSessionMapper
import org.springframework.stereotype.Service

@Service
class AcademicSessionService(
    private val academicSessionMapper: AcademicSessionMapper
) {

    suspend fun findAll(): List<AcademicSession> = academicSessionMapper.findAll()

    suspend fun findById(id: Long): AcademicSession? = academicSessionMapper.findById(id)

    suspend fun findCurrent(): AcademicSession? = academicSessionMapper.findCurrentSession()

    suspend fun save(session: AcademicSession) {
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