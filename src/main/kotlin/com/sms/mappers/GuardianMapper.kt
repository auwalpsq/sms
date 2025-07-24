package com.sms.mappers

import com.sms.entities.Guardian
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface GuardianMapper {
    fun insertIntoPersons(guardian: Guardian): Int
    fun insertIntoContactDetails(guardian: Guardian): Int
    fun insertIntoGuardians(guardian: Guardian): Int
    fun update(guardian: Guardian): Int
    fun delete(@Param("id") id: Long): Int
    fun findById(@Param("id") id: Long?): Guardian?
    fun findAll(): List<Guardian>
    fun existsByEmail(@Param("email") email: String): Boolean
}