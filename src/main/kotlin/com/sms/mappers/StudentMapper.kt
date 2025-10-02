package com.sms.mappers

import com.sms.entities.Student
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface StudentMapper {
    fun save(student: Student): Int
    fun findById(@Param("id") id: Long): Student?
    fun findByAdmissionNumber(admissionNumber: String): Student?
    fun findAll(): List<Student>
    fun deleteById(id: Long): Int
    fun countAll(): Long
    fun findBySession(sessionId: Long): List<Student>
    fun findByClass(classId: Long): List<Student>

    fun findByApplicantId(applicantId: Long): Student?

    fun markAccepted(id: Long): Int
}
