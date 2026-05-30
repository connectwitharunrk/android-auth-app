package com.example.authapp.domain.repository

import com.example.authapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signup(email: String, password: String): Result<User>
    suspend fun signin(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    fun getTokenFlow(): Flow<String?>
}
