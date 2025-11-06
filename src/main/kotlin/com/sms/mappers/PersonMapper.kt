package com.sms.mappers

import com.sms.entities.Person
import org.apache.ibatis.annotations.*

@Mapper
interface PersonMapper {
    fun save(person: Person): Int
    fun update(person: Person): Int
    fun delete(id: Long): Int
    fun findById(id: Long): Person?

    // new: save and set generated id into person.id; set the person_type from 'type'
    fun saveWithType(person: Person, type: String): Int
}
