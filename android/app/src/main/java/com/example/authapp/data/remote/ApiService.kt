package com.example.authapp.data.remote

import com.example.authapp.data.remote.dto.AuthRequest
import com.example.authapp.data.remote.dto.AuthResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/signup")
    suspend fun signup(@Body request: AuthRequest): AuthResponse

    @POST("/api/auth/signin")
    suspend fun signin(@Body request: AuthRequest): AuthResponse

    @POST("/api/auth/logout")
    suspend fun logout(): AuthResponse

    @GET("/api/auth/me")
    suspend fun getCurrentUser(): AuthResponse
}
