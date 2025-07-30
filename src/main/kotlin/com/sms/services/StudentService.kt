package com.sms.services

import com.sms.entities.Student
import com.sms.mappers.StudentMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentService(private val studentMapper: StudentMapper) {

    @Transactional
    fun save(student: Student): Student {
        if(student.id == null) {
            studentMapper.insertIntoPerson(student) // inserts into persons and sets student.id
            studentMapper.save(student) // inserts into students using student.id
        }else{
            studentMapper.update(student)
        }
        return student
    }

    fun findByGuardianId(guardianId: Long): List<Student> =
        studentMapper.findByGuardianId(guardianId)

    @Transactional
    fun update(student: Student): Student {
        studentMapper.update(student)
        return student
    }

    @Transactional
    fun delete(id: Long) {
        studentMapper.delete(id)
    }
}