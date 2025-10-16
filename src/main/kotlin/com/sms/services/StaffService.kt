package com.sms.services

import com.sms.entities.Staff
import com.sms.mappers.StaffMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class StaffService(private val staffMapper: StaffMapper) {

    suspend fun findAll(): List<Staff> = withContext(Dispatchers.IO) {
        staffMapper.findAll()
    }

    suspend fun findById(id: Long): Staff? = withContext(Dispatchers.IO) {
        staffMapper.findById(id)
    }

    suspend fun save(staff: Staff) = withContext(Dispatchers.IO) {
        staffMapper.save(staff)
    }

    suspend fun update(staff: Staff) = withContext(Dispatchers.IO) {
        staffMapper.update(staff)
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        staffMapper.delete(id)
    }
}