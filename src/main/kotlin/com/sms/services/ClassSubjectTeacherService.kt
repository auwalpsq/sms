package com.sms.services

import com.sms.entities.ClassSubjectTeacher
import com.sms.mappers.ClassSubjectTeacherMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class ClassSubjectTeacherService(private val mapper: ClassSubjectTeacherMapper) {

    suspend fun findAll(): List<ClassSubjectTeacher> = withContext(Dispatchers.IO) {
        mapper.findAll()
    }

    suspend fun findById(id: Long): ClassSubjectTeacher? = withContext(Dispatchers.IO) {
        mapper.findById(id)
    }

    suspend fun save(assignment: ClassSubjectTeacher) = withContext(Dispatchers.IO) {
        mapper.save(assignment)
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        mapper.delete(id)
    }
}