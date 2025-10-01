package com.example.mnotepad.network

import com.example.mnotepad.entities.models.UserDetail
import com.example.mnotepad.entities.models.UserResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL =
    "https://dummyjson.com"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

interface UsersApiService {
    @GET("/users")
    suspend fun getUsers(): UserResponse

    @GET("/users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): UserDetail
}

object UsersApi {
    val retrofitService: UsersApiService by lazy {
        retrofit.create(UsersApiService::class.java)
    }
}
