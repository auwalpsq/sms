package com.sms.services

import com.sms.entities.Applicant
import com.sms.mappers.ApplicantMapper
import com.sms.util.ApplicationNumberGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class ApplicantService(
    private val applicantMapper: ApplicantMapper
) {

    @Transactional
    suspend fun save(applicant: Applicant): Applicant = withContext(Dispatchers.IO) {
        if (applicant.id == 0L) {
            val latestAppNumber = applicantMapper.findLatestApplicationNumberForToday()
            applicant.applicationNumber = ApplicationNumberGenerator.generate(latestAppNumber)

            applicant.submissionDate = LocalDate.now()
            applicant.applicationStatus = Applicant.ApplicationStatus.PENDING
            applicant.paymentStatus = Applicant.PaymentStatus.UNPAID

            applicantMapper.insertIntoPerson(applicant)
            applicantMapper.insertIntoApplicant(applicant)
        } else {
            // Existing applicant: just update
            applicantMapper.update(applicant)
        }
        applicant
    }

    suspend fun findByGuardianId(guardianId: Long): List<Applicant> = withContext(Dispatchers.IO) {
        applicantMapper.findByGuardianId(guardianId)
    }

    @Transactional
    suspend fun update(applicant: Applicant): Applicant = withContext(Dispatchers.IO) {
        applicantMapper.update(applicant)
        applicant
    }

    @Transactional
    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        applicantMapper.delete(id)
    }

    suspend fun findByStatus(status: Applicant.ApplicationStatus): List<Applicant> {
        return applicantMapper.findByStatus(status.name)
    }

    suspend fun findAll(): List<Applicant> = withContext(Dispatchers.IO) {
        applicantMapper.findAll()
    }

    open suspend fun findByOptionalStatus(status: Applicant.ApplicationStatus?): List<Applicant> = withContext(
        Dispatchers.IO){
        applicantMapper.findByOptionalStatus(status)
    }
    open suspend fun findById(id: Long): Applicant? = withContext(Dispatchers.IO){
        applicantMapper.findById(id)
    }
    suspend fun updatePaymentStatus(reference: String, status: String) {
        applicantMapper.updatePaymentStatus(reference, status)
    }
}