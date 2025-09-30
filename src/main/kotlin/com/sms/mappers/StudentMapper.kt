package com.sms.mappers

import com.sms.entities.Student
import org.apache.ibatis.annotations.Mapper

@Mapper
interface StudentMapper {
    fun save(student: Student): Int
    fun findById(id: Long): Student?
    fun findByAdmissionNumber(admissionNumber: String): Student?
    fun findAll(): List<Student>
    fun deleteById(id: Long): Int
    fun countAll(): Long
    fun findBySession(sessionId: Long): List<Student>
    fun findByClass(classId: Long): List<Student>

    fun findByApplicantId(applicantId: Long): Student?
}
