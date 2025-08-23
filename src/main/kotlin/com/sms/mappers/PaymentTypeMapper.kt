package com.sms.mappers

import com.sms.entities.PaymentType
import com.sms.enums.PaymentCategory
import org.apache.ibatis.annotations.Mapper

@Mapper
interface PaymentTypeMapper {

    fun findAll(): List<PaymentType>

    fun findById(id: Long): PaymentType?

    fun save(paymentType: PaymentType)

    fun update(paymentType: PaymentType)

    fun delete(id: Long)

    fun findByCategory(category: PaymentCategory): PaymentType?
}