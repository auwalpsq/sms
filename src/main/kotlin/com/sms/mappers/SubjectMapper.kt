package com.sms.mappers

import com.sms.entities.Subject
import org.apache.ibatis.annotations.*

@Mapper
interface SubjectMapper {

    @Select("SELECT * FROM subject WHERE id = #{id}")
    suspend fun findById(id: Long): Subject?

    @Select("SELECT * FROM subject")
    suspend fun findAll(): List<Subject>

    fun save(subject: Subject): Int

    @Update("""
        UPDATE subject 
        SET name = #{name}, code = #{code}, section = #{section}
        WHERE id = #{id}
    """)
    suspend fun update(subject: Subject): Int

    @Delete("DELETE FROM subject WHERE id = #{id}")
    suspend fun delete(id: Long): Int
}