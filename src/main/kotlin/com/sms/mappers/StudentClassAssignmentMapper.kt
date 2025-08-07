package com.sms.mappers

import com.sms.entities.StudentClassAssignment
import org.apache.ibatis.annotations.*

@Mapper
interface StudentClassAssignmentMapper {

    suspend fun save(assignment: StudentClassAssignment)

    suspend fun findByStudentIdAndSessionId(studentId: Long, sessionId: Long): StudentClassAssignment?

    suspend fun findAllByClassIdAndSessionId(classId: Long, sessionId: Long): List<StudentClassAssignment>

    suspend fun deleteById(id: Long)
}