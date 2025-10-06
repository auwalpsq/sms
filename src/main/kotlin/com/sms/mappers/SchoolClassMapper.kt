package com.sms.mappers

import com.sms.entities.SchoolClass
import com.sms.enums.Section
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface SchoolClassMapper {

    fun findAll(): List<SchoolClass>

    fun findById(@Param("id") id: Long): SchoolClass?

    fun save(schoolClass: SchoolClass)

    fun update(schoolClass: SchoolClass)

    fun deleteById(@Param("id") id: Long)

    fun existsByUniqueFields(
        @Param("name") name: String,
        @Param("level") level: String,  // Enum stored as STRING in DB
        @Param("grade") grade: String,
        @Param("section") section: String
    ): Boolean

    fun findBySection(name: Section): List<String>

    fun findFullBySection(section: Section): List<SchoolClass>

}