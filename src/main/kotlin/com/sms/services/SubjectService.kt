package com.sms.services

import com.sms.entities.Subject
import com.sms.mappers.SubjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class SubjectService(private val subjectMapper: SubjectMapper) {

    suspend fun findAll(): List<Subject> = withContext(Dispatchers.IO) {
        subjectMapper.findAll()
    }

    suspend fun findById(id: Long): Subject? = withContext(Dispatchers.IO) {
        subjectMapper.findById(id)
    }

    suspend fun save(subject: Subject) = withContext(Dispatchers.IO) {
        subjectMapper.save(subject)
    }

    suspend fun update(subject: Subject) = withContext(Dispatchers.IO) {
        subjectMapper.update(subject)
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        subjectMapper.delete(id)
    }
}