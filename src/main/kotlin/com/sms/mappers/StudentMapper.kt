package com.sms.mappers

import com.sms.entities.Student
import org.apache.ibatis.annotations.Mapper

@Mapper
interface StudentMapper {
    suspend fun save(student: Student): Int
    suspend fun findById(id: Long): Student?
    suspend fun findByAdmissionNumber(admissionNumber: String): Student?
    suspend fun findAll(): List<Student>
    suspend fun deleteById(id: Long): Int
    suspend fun countAll(): Long
    suspend fun findBySession(sessionId: Long): List<Student>
    suspend fun findByClass(classId: Long): List<Student>
}
