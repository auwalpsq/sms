package com.sms.mappers

import com.sms.entities.Student
import org.apache.ibatis.annotations.*

@Mapper
interface StudentMapper {

    //@Select("SELECT * FROM students WHERE guardian_id = #{guardianId}")
    fun findByGuardianId(guardianId: Long): List<Student>

    //@Insert("SQL in XML")
    //@Options(useGeneratedKeys = true, keyProperty = "id")
    fun save(student: Student)

    fun update(student: Student)

    //@Delete("DELETE FROM students WHERE id = #{id}")
    fun delete(id: Long)
}