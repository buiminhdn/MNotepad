package com.example.mnotepad.repositories

import com.example.mnotepad.network.UsersApiService
import javax.inject.Inject

class UserRepository @Inject constructor(private val api: UsersApiService) {
    suspend fun getUsers() = api.getUsers()

    suspend fun getUserById(userId: Int) = api.getUserById(userId)
}
