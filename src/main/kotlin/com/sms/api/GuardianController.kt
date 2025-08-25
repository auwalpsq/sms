package com.sms.api

import com.sms.entities.Guardian
import com.sms.services.GuardianService
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.*

@RolesAllowed("GUARDIAN", "ADMIN")
@RestController
@RequestMapping("/api/guardians")
class GuardianController(
    private val guardianService: GuardianService
) {
    @GetMapping
    suspend fun getAllGuardians(): List<Guardian> {
        return guardianService.findAll()
    }

    @GetMapping("/{id}")
    suspend fun getGuardianById(@PathVariable id: Long): Guardian? {
        return guardianService.findById(id)
    }

    @GetMapping("/search")
    suspend fun getGuardianByEmail(@RequestParam email: String): Guardian? {
        return guardianService.findByEmail(email)
    }

    @PostMapping
    suspend fun createGuardian(@RequestBody guardian: Guardian): Guardian {
        return guardianService.save(guardian)
    }

    @PutMapping("/{id}")
    suspend fun updateGuardian(@RequestBody guardian: Guardian): Guardian {
        return guardianService.save(guardian)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteGuardian(@RequestBody guardian: Guardian) {
        guardianService.delete(guardian)
    }
}