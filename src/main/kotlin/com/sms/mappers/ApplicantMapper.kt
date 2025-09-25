package com.sms.mappers

import com.sms.entities.Applicant
import org.apache.ibatis.annotations.*
import org.springframework.expression.spel.ast.BeanReference
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

    @Update("UPDATE applicants SET photo_url = #{photoUrl} WHERE id = #{id}")
    fun updatePhoto(@Param("id") id: Long, @Param("photoUrl") photoUrl: String)

    suspend fun findLatestApplicationNumberForToday(@Param("datePrefix") datePrefix: String = LocalDate.now().format(
        DateTimeFormatter.ofPattern("yyyyMMdd"))): String?

    fun findAll(): List<Applicant>

    fun findByOptionalStatus(status: Applicant.ApplicationStatus?): List<Applicant>

    fun findById(@Param("id") id: Long): Applicant?

    fun updatePaymentStatus(reference: String, status: String)

    suspend fun countByGuardian(guardianId: Long): Int

}