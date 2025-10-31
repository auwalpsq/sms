package com.sms.services

import com.sms.broadcast.UiBroadcaster
import com.sms.entities.Applicant
import com.sms.mappers.ApplicantMapper
import com.sms.util.ApplicationNumberGenerator
import com.sms.util.PageResult
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

            UiBroadcaster.broadcast(
                "NEW_APPLICATION",
                mapOf(
                    "appNumber" to applicant.applicationNumber,
                    "status" to applicant.applicationStatus.name
                )
            )

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
    suspend fun countByGuardian(guardianId: Long): Int = withContext(Dispatchers.IO) {
        applicantMapper.countByGuardian(guardianId)
    }

    // 🔹 New dedicated methods
    @Transactional
    suspend fun approveApplicant(id: Long) = withContext(Dispatchers.IO) {
        applicantMapper.approveApplicant(id)
        val applicant = applicantMapper.findById(id)
        val guardianUsername = applicant?.guardian!!.email

        UiBroadcaster.broadcastToUser(
            guardianUsername,
            "APPLICATION_APPROVED",
            mapOf("applicantName" to applicant!!.getFullName())
        )
        UiBroadcaster.broadcastToUser(
            guardianUsername,
            "APPLICATION_UPDATE",
            mapOf("applicantName" to applicant!!.getFullName())
        )
    }

    @Transactional
    suspend fun rejectApplicant(id: Long) = withContext(Dispatchers.IO) {
        applicantMapper.rejectApplicant(id)
        val applicant = applicantMapper.findById(id)

        val guardianUsername = applicant?.guardian!!.email

        UiBroadcaster.broadcastToUser(
            guardianUsername,
            "APPLICATION_REJECTED",
            mapOf("applicantName" to applicant!!.getFullName())
        )
        UiBroadcaster.broadcastToUser(
            guardianUsername,
            "APPLICATION_UPDATE",
            mapOf("applicantName" to applicant!!.getFullName())
        )
    }
    @Transactional
    suspend fun resetApplicantToPending(applicantId: Long) = withContext(Dispatchers.IO) {
        val applicant = applicantMapper.findById(applicantId)
            ?: throw IllegalArgumentException("Applicant not found")

        if (applicant.paymentStatus != Applicant.PaymentStatus.UNPAID) {
            throw IllegalStateException("Cannot reset application after payment has been made")
        }

        applicantMapper.resetApplicantToPending(applicantId)
        val guardianUsername = applicant?.guardian!!.email

        UiBroadcaster.broadcastToUser(
            guardianUsername,
            "APPLICATION_RESET",
            mapOf("applicantName" to applicant!!.getFullName())
        )

        UiBroadcaster.broadcastToUser(
            guardianUsername,
            "APPLICATION_UPDATE",
            mapOf("applicantName" to applicant!!.getFullName())
        )
    }

    suspend fun searchApplicants(query: String, status: Applicant.ApplicationStatus?): List<Applicant> {
        return applicantMapper.searchApplicants("%$query%", status)
    }
    suspend fun findPageByStatus(
        status: Applicant.ApplicationStatus?,
        page: Int,
        pageSize: Int
    ): PageResult<Applicant> {
        val offset = (page - 1) * pageSize
        val items = applicantMapper.findPageByStatus(status, offset, pageSize)
        val totalCount = applicantMapper.countByStatus(status)
        return PageResult(items, totalCount)
    }
    suspend fun searchApplicantsPaginated(
        query: String,
        status: Applicant.ApplicationStatus?,
        page: Int,
        pageSize: Int
    ): PageResult<Applicant> {
        val offset = (page - 1) * pageSize
        val items = applicantMapper.searchApplicantsPaginated("%$query%", status, offset, pageSize)
        val totalCount = applicantMapper.countSearchApplicants("%$query%", status)
        return PageResult(items, totalCount)
    }

}