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
    fun findByEmail(email: String): Guardian?

    // Paged list
    fun findPage(
        @Param("offset") offset: Int,
        @Param("size") size: Int
    ): List<Guardian>

    // Paged search
    fun findPageBySearch(
        @Param("query") query: String,
        @Param("offset") offset: Int,
        @Param("size") size: Int
    ): List<Guardian>

    // Non-paged search (optional)
    fun search(@Param("query") query: String): List<Guardian>

    // Optional count helper
    fun countAll(): Int
}