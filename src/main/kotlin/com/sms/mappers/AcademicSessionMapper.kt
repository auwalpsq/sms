package com.sms.mappers

import com.sms.entities.AcademicSession
import com.sms.enums.Term
import org.apache.ibatis.annotations.*

@Mapper
interface AcademicSessionMapper {

    fun findAll(): List<AcademicSession>

    fun findById(id: Long): AcademicSession

    fun findCurrentSession(): AcademicSession

    fun findByYearAndTerm(@Param("startYear") startYear: Int, @Param("term") term: Term): AcademicSession?

    fun save(@Param("session") session: AcademicSession)

    fun update(session: AcademicSession)

    fun deleteById(id: Long)
}