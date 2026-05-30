package com.example.authapp.domain.usecase

import com.example.authapp.domain.model.User
import com.example.authapp.domain.repository.AuthRepository
import javax.inject.Inject

class SignupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return authRepository.signup(email, password)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
