package com.sms.api

import com.sms.entities.Guardian
import com.sms.mappers.GuardianMapper
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.*

@RolesAllowed("GUARDIAN")
@RestController
@RequestMapping("/api/guardians")
class GuardianController(
    private val guardianMapper: GuardianMapper
) {

    @GetMapping
    fun getAllGuardians(): List<Guardian> {
        return guardianMapper.findAll()
    }

    @GetMapping("/{id}")
    fun getGuardianById(@PathVariable id: Long): Guardian? {
        return guardianMapper.findById(id)
    }

    @GetMapping("/search")
    fun getGuardianByEmail(@RequestParam email: String): Guardian? {
        return guardianMapper.findByEmail(email)
    }

    @PostMapping
    fun createGuardian(@RequestBody guardian: Guardian): Guardian {
        guardianMapper.insertIntoPersons(guardian)
        guardianMapper.insertIntoContactDetails(guardian)
        guardianMapper.insertIntoGuardians(guardian)
        return guardian
    }

    @PutMapping("/{id}")
    fun updateGuardian(@RequestBody guardian: Guardian): Guardian {
        guardianMapper.update(guardian)
        return guardian
    }

    @DeleteMapping("/{id}")
    fun deleteGuardian(@PathVariable id: Long) {
        guardianMapper.delete(id)
    }
}