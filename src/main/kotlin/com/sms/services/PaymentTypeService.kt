package com.sms.services

import com.sms.entities.PaymentType
import com.sms.enums.PaymentCategory
import com.sms.mappers.PaymentTypeMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

@Service
class PaymentTypeService(private val paymentTypeMapper: PaymentTypeMapper) {

    suspend fun findAll(): List<PaymentType> = withContext(Dispatchers.IO) {
        paymentTypeMapper.findAll()
    }

    suspend fun findById(id: Long): PaymentType? = withContext(Dispatchers.IO) {
        paymentTypeMapper.findById(id)
    }

    suspend fun save(paymentType: PaymentType) = withContext(Dispatchers.IO) {
        try {
            if (paymentType.id == null) {
                paymentTypeMapper.save(paymentType)
            } else {
                paymentTypeMapper.update(paymentType)
            }
        } catch (ex: DuplicateKeyException) {
            throw IllegalArgumentException(
                "Payment Type with category '${paymentType.category}' already exists",
                ex
            )
        }
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        paymentTypeMapper.delete(id)
    }

    suspend fun existsByCategory(category: PaymentCategory): Boolean = withContext(Dispatchers.IO) {
        paymentTypeMapper.findByCategory(category) != null
    }
    open suspend fun findByCategory(category: PaymentCategory): PaymentType? =
        withContext(Dispatchers.IO){
            paymentTypeMapper.findByCategory(category)
        }
}