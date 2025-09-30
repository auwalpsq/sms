package com.sms.services

import com.sms.entities.Student
import com.sms.mappers.StudentMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentService(
    private val studentMapper: StudentMapper
) {

    @Transactional
    suspend fun save(student: Student): Student = withContext(Dispatchers.IO) {
        studentMapper.save(student)
        student
    }

    suspend fun findById(id: Long): Student? = withContext(Dispatchers.IO) {
        studentMapper.findById(id)
    }

    suspend fun findByAdmissionNumber(admissionNumber: String): Student? =
        withContext(Dispatchers.IO) { studentMapper.findByAdmissionNumber(admissionNumber) }

    suspend fun findAll(): List<Student> = withContext(Dispatchers.IO) {
        studentMapper.findAll()
    }

    suspend fun findBySession(sessionId: Long): List<Student> = withContext(Dispatchers.IO) {
        studentMapper.findBySession(sessionId)
    }

    suspend fun findByClass(classId: Long): List<Student> = withContext(Dispatchers.IO) {
        studentMapper.findByClass(classId)
    }

    @Transactional
    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        studentMapper.deleteById(id)
    }

    suspend fun countAll(): Long = withContext(Dispatchers.IO) {
        studentMapper.countAll()
    }
}
