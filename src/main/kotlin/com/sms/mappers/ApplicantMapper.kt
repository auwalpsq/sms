package com.sms.mappers

import com.sms.entities.Applicant
import org.apache.ibatis.annotations.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Mapper
interface ApplicantMapper {

    fun findByGuardianId(guardianId: Long): List<Applicant>

    fun findByStatus(status: String): List<Applicant>

    fun insertIntoPerson(applicant: Applicant)

    fun insertIntoApplicant(applicant: Applicant)

    fun update(applicant: Applicant)

    fun delete(id: Long)

    suspend fun findLatestApplicationNumberForToday(@Param("datePrefix") datePrefix: String = LocalDate.now().format(
        DateTimeFormatter.ofPattern("yyyyMMdd"))): String?
}