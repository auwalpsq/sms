package com.sms.mappers

import com.sms.entities.Staff
import org.apache.ibatis.annotations.*

@Mapper
interface StaffMapper {

    @Select("SELECT * FROM staff WHERE id = #{id}")
    suspend fun findById(id: Long): Staff?

    @Select("SELECT * FROM staff")
    suspend fun findAll(): List<Staff>

    fun save(staff: Staff): Int

    @Update("""
        UPDATE staff 
        SET staff_id = #{staffId}, staff_type = #{staffType}, qualification = #{qualification}, 
            employment_date = #{employmentDate}
        WHERE id = #{id}
    """)
    suspend fun update(staff: Staff): Int

    @Delete("DELETE FROM staff WHERE id = #{id}")
    suspend fun delete(id: Long): Int
}