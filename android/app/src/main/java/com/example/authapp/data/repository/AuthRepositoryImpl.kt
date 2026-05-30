package com.example.authapp.data.repository

import com.example.authapp.data.local.TokenManager
import com.example.authapp.data.remote.ApiService
import com.example.authapp.data.remote.dto.AuthRequest
import com.example.authapp.domain.model.User
import com.example.authapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun signup(email: String, password: String): Result<User> = try {
        val response = apiService.signup(AuthRequest(email, password))
        if (response.success && response.data != null && response.token != null) {
            tokenManager.saveToken(response.token)
            tokenManager.saveUserInfo(response.data.id, response.data.email)
            Result.success(User(
                id = response.data.id,
                email = response.data.email,
                name = response.data.name ?: ""
            ))
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signin(email: String, password: String): Result<User> = try {
        val response = apiService.signin(AuthRequest(email, password))
        if (response.success && response.data != null && response.token != null) {
            tokenManager.saveToken(response.token)
            tokenManager.saveUserInfo(response.data.id, response.data.email)
            Result.success(User(
                id = response.data.id,
                email = response.data.email,
                name = response.data.name ?: ""
            ))
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun logout(): Result<Unit> = try {
        apiService.logout()
        tokenManager.clearToken()
        Result.success(Unit)
    } catch (e: Exception) {
        tokenManager.clearToken()
        Result.success(Unit)
    }

    override fun getTokenFlow(): Flow<String?> = tokenManager.tokenFlow
}
