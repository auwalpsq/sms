package com.sms.services

import com.sms.entities.Student
import com.sms.mappers.StudentMapper
import org.springframework.stereotype.Service

@Service
class StudentService(
    private val studentMapper: StudentMapper
) {
    fun findByGuardianId(guardianId: Long): List<Student> =
        studentMapper.findByGuardianId(guardianId)

    fun save(student: Student) {
        if (student.id == null) {
            studentMapper.save(student)
        } else {
            studentMapper.update(student)
        }
    }

    fun delete(id: Long) {
        studentMapper.delete(id)
    }
}