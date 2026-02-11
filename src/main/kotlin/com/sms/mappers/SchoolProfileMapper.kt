package com.sms.mappers

import com.sms.entities.SchoolProfile
import org.apache.ibatis.annotations.Mapper

@Mapper
interface SchoolProfileMapper {
    fun findAll(): List<SchoolProfile>
    fun findById(id: Long): SchoolProfile?
    fun insert(profile: SchoolProfile): Int
    fun update(profile: SchoolProfile)
}
