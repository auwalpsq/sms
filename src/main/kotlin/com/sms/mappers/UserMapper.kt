package com.sms.mappers

import com.sms.entities.Role
import com.sms.entities.User
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface UserMapper {
    fun findByUsername(username: String): User?
    fun findById(id: Long): User?
    fun existsByUsername(username: String): Boolean
    fun insertUser(user: User): Int
    fun updateUser(user: User): Int
    fun updateUserPassword(@Param("id") id: Long, @Param("password") password: String): Int
    fun deleteUser(username: String): Int
    fun findRolesByUserId(userId: Long): List<Role>
    fun findAllUsers(): List<User>
    fun addRoleToUser(@Param("userId") userId: Long, @Param("roleId") roleId: Long): Int
    fun removeRoleFromUser(@Param("userId") userId: Long, @Param("roleId") roleId: Long): Int
    fun countUsers(): Int
}