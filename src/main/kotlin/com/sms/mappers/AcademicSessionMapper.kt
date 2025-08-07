package com.sms.mappers

import com.sms.entities.AcademicSession
import org.apache.ibatis.annotations.*

@Mapper
interface AcademicSessionMapper {

    suspend fun findAll(): List<AcademicSession>

    suspend fun findById(id: Long): AcademicSession?

    suspend fun findCurrentSession(): AcademicSession?

    suspend fun save(session: AcademicSession)

    suspend fun update(session: AcademicSession)

    suspend fun deleteById(id: Long)
}