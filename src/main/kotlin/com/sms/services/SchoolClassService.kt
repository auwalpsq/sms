package com.sms.services

import com.sms.entities.SchoolClass
import com.sms.enums.Section
import com.sms.mappers.SchoolClassMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class SchoolClassService(
    private val schoolClassMapper: SchoolClassMapper
) {

    suspend fun findAll(): List<SchoolClass> = withContext(Dispatchers.IO) {
        schoolClassMapper.findAll()
    }

    suspend fun findById(id: Long): SchoolClass? = withContext(Dispatchers.IO) {
        schoolClassMapper.findById(id)
    }

    suspend fun save(schoolClass: SchoolClass) = withContext(Dispatchers.IO) {
        // Auto-generate class name using section + level.number + grade
        val generatedName = "${schoolClass.section.name} ${schoolClass.level.number} ${schoolClass.grade.name}"
        val classToSave = schoolClass.copy(name = generatedName)

        // Check duplicates before inserting
        val exists = schoolClassMapper.existsByUniqueFields(
            name = generatedName,
            level = schoolClass.level.name,  // stored as string in DB
            grade = schoolClass.grade.name,
            section = schoolClass.section.name
        )

        if (schoolClass.id == 0L) {
            if (exists) {
                throw IllegalArgumentException("Class '$generatedName' already exists")
            }
            schoolClassMapper.save(classToSave)
        } else {
            // For updates, check if duplicate exists but allow updating itself
            if (exists) {
                val current = schoolClassMapper.findById(schoolClass.id)
                if (current != null &&
                    (current.section != schoolClass.section ||
                            current.level != schoolClass.level ||
                            current.grade != schoolClass.grade)
                ) {
                    throw IllegalArgumentException("Class '$generatedName' already exists")
                }
            }

            schoolClassMapper.update(classToSave)
        }
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        schoolClassMapper.deleteById(id)
    }
    suspend fun findBySection(section: Section): List<String> {
        return schoolClassMapper.findBySection(section.name)
    }
}