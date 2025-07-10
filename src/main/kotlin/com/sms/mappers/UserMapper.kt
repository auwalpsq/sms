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
}


@Mapper
interface RoleMapper {
    fun findById(id: Long): Role?
    fun findByName(name: String): Role?
    fun findAll(): List<Role>
    fun insertRole(role: Role): Int
    fun updateRole(role: Role): Int
    fun deleteRole(id: Long): Int

    fun findRolesByUserId(userId: Long): List<Role>
    fun addRoleToUser(userId: Long, roleId: Long): Int
    fun removeRoleFromUser(userId: Long, roleId: Long): Int
    fun addRolesToUser(userId: Long, roleIds: List<Long>): Int

    fun countRoles(): Int
    fun roleExists(id: Long): Boolean
    fun roleNameExists(name: String): Boolean
}