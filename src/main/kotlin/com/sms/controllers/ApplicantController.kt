package com.sms.controllers

import com.sms.entities.Applicant
import com.sms.mappers.ApplicantMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/api/applicants")
class ApplicantController(
    private val applicantMapper: ApplicantMapper
) {
    private val uploadDir = "uploads/photos/"

    @PostMapping("/{id}/upload-photo")
    fun uploadPhoto(
        @PathVariable id: Long,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        try {
            // Ensure upload folder exists
            val dir = File(uploadDir)
            if (!dir.exists()) dir.mkdirs()

            val fileName = "${id}_${file.originalFilename}"
            val path = Paths.get("$uploadDir$fileName")
            Files.write(path, file.bytes)

            // Update applicant in DB
            val applicant = Applicant(photoUrl = "/photos/$fileName")
            //applicant.id = id
            applicantMapper.update(applicant)

            return ResponseEntity.ok("Photo uploaded successfully")
        } catch (e: Exception) {
            return ResponseEntity.status(500).body("Error uploading photo: ${e.message}")
        }
    }
}