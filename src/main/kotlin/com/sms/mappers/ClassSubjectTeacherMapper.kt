package com.sms.mappers

import com.sms.entities.ClassSubjectTeacher
import org.apache.ibatis.annotations.*

@Mapper
interface ClassSubjectTeacherMapper {

    @Select("SELECT * FROM class_subject_teacher WHERE id = #{id}")
    suspend fun findById(id: Long): ClassSubjectTeacher?

    @Select("SELECT * FROM class_subject_teacher")
    suspend fun findAll(): List<ClassSubjectTeacher>

    fun save(assignment: ClassSubjectTeacher): Int

    @Delete("DELETE FROM class_subject_teacher WHERE id = #{id}")
    suspend fun delete(id: Long): Int
}