package com.sms.services

import com.sms.entities.ContactPerson
import com.sms.mappers.ContactPersonMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class ContactPersonService(
    private val contactPersonMapper: ContactPersonMapper
) {


    suspend fun save(contact: ContactPerson): Int = withContext(Dispatchers.IO) {
        contactPersonMapper.save(contact)
    }

    suspend fun update(contact: ContactPerson): Int = withContext(Dispatchers.IO) {
        contactPersonMapper.update(contact)
    }

    suspend fun findById(id: Long): ContactPerson? = withContext(Dispatchers.IO) {
        contactPersonMapper.findById(id)
    }

    suspend fun delete(id: Long): Int = withContext(Dispatchers.IO) {
        contactPersonMapper.delete(id)
    }

    suspend fun findByEmail(email: String): ContactPerson? = withContext(Dispatchers.IO) {
        contactPersonMapper.findByEmail(email)
    }

    suspend fun emailExists(email: String): Boolean = withContext(Dispatchers.IO) {
        contactPersonMapper.emailExists(email)
    }
}