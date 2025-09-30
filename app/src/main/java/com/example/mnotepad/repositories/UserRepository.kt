package com.example.mnotepad.repositories

import com.example.mnotepad.entities.models.User
import com.example.mnotepad.network.UsersApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UserRepository(private val api: UsersApiService) {
    fun getUsers(): Flow<List<User>> = flow {
        val response = api.getUsers()
        emit(response.users)
    }.flowOn(Dispatchers.IO)
}
