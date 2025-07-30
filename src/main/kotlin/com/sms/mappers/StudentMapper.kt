package com.sms.mappers

import com.sms.entities.Student
import org.apache.ibatis.annotations.*

@Mapper
interface StudentMapper {
    fun insertIntoPerson(student: Student): Int
    fun save(student: Student): Int
    fun update(student: Student): Int
    fun delete(id: Long): Int
    fun findByGuardianId(guardianId: Long): List<Student>
}