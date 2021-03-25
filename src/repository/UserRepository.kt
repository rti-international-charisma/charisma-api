package com.rti.charisma.api.repository

import com.rti.charisma.api.db.tables.SecurityQuestion
import com.rti.charisma.api.db.tables.User
import com.rti.charisma.api.route.Signup


interface UserRepository {

    fun getSecurityQuestions(secQId: Int?): List<SecurityQuestion>

    fun doesUserExist(username: String): Boolean
    fun registerUser(signup: Signup): Int
    fun findByUserByUsername(username: String): User?
}