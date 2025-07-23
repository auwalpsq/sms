package com.sms.mappers

import com.sms.entities.Role
import com.sms.entities.User
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.util.*

@Mapper
interface UserMapper {
    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean
    fun insertUser(user: User): Int
    fun updateUser(user: User): Int
    fun deleteUser(username: String): Int
    fun findRolesByUsername(username: String): Set<Role>
    fun findAllUsers(): List<User>
}