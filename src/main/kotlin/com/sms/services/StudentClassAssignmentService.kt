package com.sms.services

import com.sms.entities.StudentClassAssignment
import com.sms.mappers.StudentClassAssignmentMapper
import com.sms.mappers.StudentMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentClassAssignmentService(
    private val mapper: StudentClassAssignmentMapper,
    private val studentMapper: StudentMapper
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
        val assignment = mapper.findById(id)
            ?: throw IllegalArgumentException("Assignment not found")

        val student = studentMapper.findById(assignment.student!!.id!!)
            ?: throw IllegalArgumentException("Student not found")

        if (student.admissionAccepted) {
            throw IllegalStateException("Cannot drop assignment. Guardian has already accepted admission.")
        }

        // Delete the assignment
        mapper.deleteById(id)

        // Also remove the student record
        studentMapper.deleteById(student.id!!)
    }
}