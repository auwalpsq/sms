package com.sms.mappers

import com.sms.entities.ContactPerson
import org.apache.ibatis.annotations.Mapper

@Mapper
interface ContactPersonMapper {

    fun save(contact: ContactPerson): Int

    fun update(contact: ContactPerson): Int

    fun findById(id: Long): ContactPerson?

    fun delete(id: Long): Int
}
