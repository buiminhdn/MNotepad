package com.example.mnotepad.network

import com.example.mnotepad.entities.models.UserDetail
import com.example.mnotepad.entities.models.UserResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface UsersApiService {
    @GET("/users")
    suspend fun getUsers(): UserResponse

    @GET("/users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): UserDetail
}