package com.sms.mappers

import com.sms.entities.Staff
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface StaffMapper {

    fun save(staff: Staff): Int

    fun update(staff: Staff): Int

    fun findById(@Param("id") id: Long): Staff?

    fun findAll(
        @Param("search") search: String?,
        @Param("offset") offset: Int,
        @Param("limit") limit: Int
    ): List<Staff>

    fun countAll(@Param("search") search: String?): Int

    fun delete(@Param("id") id: Long): Int

    fun findByEmail(email: String): Staff?

}