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
        val studentId = assignment.student?.id
            ?: throw IllegalArgumentException("Student is required")
        val sessionId = assignment.academicSession?.id
            ?: throw IllegalArgumentException("Academic session is required")

        val existing = mapper.findByStudentIdAndSessionId(studentId, sessionId)
        if (existing != null) {
            throw IllegalStateException("This student is already assigned to a class for the selected session.")
        }

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