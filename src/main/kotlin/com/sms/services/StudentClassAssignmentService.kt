package com.sms.services

import com.sms.entities.StudentClassAssignment
import com.sms.mappers.StudentClassAssignmentMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentClassAssignmentService(
    private val mapper: StudentClassAssignmentMapper
) {
    @Transactional
    suspend fun assignStudent(assignment: StudentClassAssignment) {
        mapper.save(assignment)
    }

    suspend fun findAssignment(studentId: Long, sessionId: Long): StudentClassAssignment? {
        return mapper.findByStudentIdAndSessionId(studentId, sessionId)
    }

    suspend fun getAssignmentsForClass(classId: Long, sessionId: Long): List<StudentClassAssignment> {
        return mapper.findAllByClassIdAndSessionId(classId, sessionId)
    }

    @Transactional
    suspend fun deleteAssignment(id: Long) {
        mapper.deleteById(id)
    }
}