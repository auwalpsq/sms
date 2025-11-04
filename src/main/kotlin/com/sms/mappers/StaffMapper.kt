package com.sms.mappers

import com.sms.entities.Staff
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface StaffMapper {

    fun save(staff: Staff): Int

    suspend fun update(staff: Staff): Int

    suspend fun findById(@Param("id") id: Long): Staff?

    suspend fun findAll(
        @Param("search") search: String?,
        @Param("offset") offset: Int,
        @Param("limit") limit: Int
    ): List<Staff>

    suspend fun countAll(@Param("search") search: String?): Int

    suspend fun delete(@Param("id") id: Long): Int
}