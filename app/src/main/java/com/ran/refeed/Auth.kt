// Auth.kt
package com.ran.refeed

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.tasks.await

// Make this an interface, as specified in the previous response.
interface Auth {
    suspend fun loginUser(email: String, password: String): Result<Unit>
    suspend fun registerUser(email: String, password: String): Result<Unit>
}


class FirebaseAuthRepository(private val auth: FirebaseAuth) : Auth { // Implement the interface
    override suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                when (e) {
                    is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> Exception(
                        "Invalid email or password"
                    ) // Handle invalid user

                    else -> Exception("Login failed. Please try again.")
                }
            )
        }
    }

    override suspend fun registerUser(email: String, password: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                when (e) {
                    is FirebaseAuthUserCollisionException -> Exception("User already exists")
                    else -> Exception("Registration failed. Please try again.")
                }
            )
        }
    }
}