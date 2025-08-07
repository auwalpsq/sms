package com.sms.mappers

import com.sms.entities.SchoolClass
import org.apache.ibatis.annotations.*

@Mapper
interface SchoolClassMapper {

    suspend fun findAll(): List<SchoolClass>

    suspend fun findById(id: Long): SchoolClass?

    suspend fun save(schoolClass: SchoolClass)

    suspend fun update(schoolClass: SchoolClass)

    suspend fun deleteById(id: Long)
}