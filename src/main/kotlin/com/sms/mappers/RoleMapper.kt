package com.sms.mappers

import com.sms.entities.Role
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param


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

    /** 🔹 Remove all roles from a user */
    fun removeAllRolesFromUser(userId: Long): Int

    fun findRolesByUsername(username: String): List<Role>

}