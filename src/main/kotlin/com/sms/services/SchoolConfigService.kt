package com.sms.services

import com.sms.entities.SchoolProfile
import com.sms.mappers.SchoolProfileMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SchoolConfigService(private val schoolProfileMapper: SchoolProfileMapper) {

    /** Get the current school profile. If none exists (first run), creates a default one. */
    @Transactional
    fun getSchoolProfile(): SchoolProfile {
        val profiles = schoolProfileMapper.findAll()
        if (profiles.isNotEmpty()) {
            return profiles[0]
        }

        // Create default profile if none exists
        val defaultProfile =
                SchoolProfile(
                        name = "School Management System",
                        motto = "Knowledge is Power",
                        address = "School Address",
                        email = "admin@school.com"
                )
        schoolProfileMapper.insert(defaultProfile)
        return defaultProfile
    }

    @Transactional
    fun updateSchoolProfile(profile: SchoolProfile): SchoolProfile {
        schoolProfileMapper.update(profile)
        return profile
    }
}
