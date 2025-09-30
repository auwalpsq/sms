package com.sms.services

import com.sms.entities.Student
import com.sms.entities.StudentClassAssignment
import com.sms.mappers.StudentMapper
import com.sms.mappers.StudentClassAssignmentMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class StudentService(
    private val studentMapper: StudentMapper,
    private val studentClassAssignmentMapper: StudentClassAssignmentMapper,
    private val applicantService: ApplicantService,
    private val academicSessionService: AcademicSessionService,
    private val schoolClassService: SchoolClassService
) {

    // 🔹 Create or update a Student and assign to class
    @Transactional
    suspend fun assignClass(applicantId: Long, classId: Long): Student = withContext(Dispatchers.IO) {
        val currentSession = academicSessionService.findCurrent()
            ?: throw IllegalStateException("No active academic session found")


        // 1. Check if student already exists
        var student = studentMapper.findByApplicantId(applicantId)

        val admittedClass = schoolClassService.findById(classId)
            ?: throw IllegalArgumentException("Class with ID $classId not found")

        if (student == null) {
            // Applicant must be approved already → create Student
            val admissionNumber = generateAdmissionNumber()
            val applicant = applicantService.findById(applicantId)
                ?: throw IllegalArgumentException("Applicant with ID $applicantId not found")


            student = Student(
                admissionNumber = admissionNumber,
                applicant = applicant,
                admittedSession = currentSession,
                admittedClass = admittedClass,
                admittedOn = LocalDate.now()
            )
            studentMapper.save(student)
        }

        // 2. Check if already assigned in this session
        val existing = studentClassAssignmentMapper.findByStudentIdAndSessionId(student.id, currentSession?.id!!)
        if (existing == null) {
            val assignment = StudentClassAssignment(
                student = student,
                schoolClass = admittedClass,
                academicSession = currentSession,
                assignedDate = LocalDateTime.now()
            )
            studentClassAssignmentMapper.save(assignment)
        }

        return@withContext student
    }

    // 🔹 Admission number generator
    private fun generateAdmissionNumber(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6) // last 6 digits
        val year = LocalDate.now().year
        return "$year-ADM-$timestamp"
    }

    // ---------------------------
    // Existing CRUD operations
    // ---------------------------

    @Transactional
    suspend fun save(student: Student): Student = withContext(Dispatchers.IO) {
        studentMapper.save(student)
        student
    }

    suspend fun findById(id: Long): Student? = withContext(Dispatchers.IO) {
        studentMapper.findById(id)
    }

    suspend fun findByAdmissionNumber(admissionNumber: String): Student? =
        withContext(Dispatchers.IO) { studentMapper.findByAdmissionNumber(admissionNumber) }

    suspend fun findAll(): List<Student> = withContext(Dispatchers.IO) {
        studentMapper.findAll()
    }

    suspend fun findBySession(sessionId: Long): List<Student> = withContext(Dispatchers.IO) {
        studentMapper.findBySession(sessionId)
    }

    suspend fun findByClass(classId: Long): List<Student> = withContext(Dispatchers.IO) {
        studentMapper.findByClass(classId)
    }

    @Transactional
    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        studentMapper.deleteById(id)
    }

    suspend fun countAll(): Long = withContext(Dispatchers.IO) {
        studentMapper.countAll()
    }
    suspend fun findByApplicantId(applicantId: Long): Student? = withContext(Dispatchers.IO) {
        studentMapper.findByApplicantId(applicantId)
    }
}