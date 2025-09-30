package com.example.mnotepad.network

import com.example.mnotepad.entities.models.User
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    fun getUsers(): Call<List<User>>
}
