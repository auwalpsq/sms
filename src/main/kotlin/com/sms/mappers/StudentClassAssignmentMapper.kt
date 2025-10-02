package com.sms.mappers

import com.sms.entities.StudentClassAssignment
import org.apache.ibatis.annotations.*

@Mapper
interface StudentClassAssignmentMapper {

    fun save(assignment: StudentClassAssignment): Int

    fun findByStudentIdAndSessionId(
        @Param("studentId") studentId: Long,
        @Param("sessionId") sessionId: Long
    ): StudentClassAssignment?

    fun findAllByClassIdAndSessionId(
        @Param("classId") classId: Long,
        @Param("sessionId") sessionId: Long
    ): List<StudentClassAssignment>


    fun deleteById(id: Long)

    fun findById(id: Long): StudentClassAssignment?
}