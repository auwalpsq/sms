package com.sms.services

import com.sms.entities.SchoolClass
import com.sms.mappers.SchoolClassMapper
import org.springframework.stereotype.Service

@Service
class SchoolClassService(
    private val schoolClassMapper: SchoolClassMapper
) {

    suspend fun findAll(): List<SchoolClass> = schoolClassMapper.findAll()

    suspend fun findById(id: Long): SchoolClass? = schoolClassMapper.findById(id)

    suspend fun save(schoolClass: SchoolClass) {
        if (schoolClass.id == 0L) {
            schoolClassMapper.save(schoolClass)
        } else {
            schoolClassMapper.update(schoolClass)
        }
    }

    suspend fun deleteById(id: Long) {
        schoolClassMapper.deleteById(id)
    }
}