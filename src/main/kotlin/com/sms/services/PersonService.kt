package com.sms.services

import com.sms.entities.Person
import com.sms.mappers.PersonMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PersonService(private val personMapper: PersonMapper) {

    fun findById(id: Long): Person? = personMapper.findById(id)

    @Transactional
    fun save(person: Person): Person {
        personMapper.save(person)
        return person
    }

    @Transactional
    fun update(person: Person): Person {
        personMapper.update(person)
        return person
    }

    @Transactional
    fun delete(id: Long) {
        personMapper.delete(id)
    }
}