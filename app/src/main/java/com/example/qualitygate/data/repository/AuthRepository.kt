package com.example.qualitygate.data.repository

import com.example.qualitygate.data.model.User
import com.example.qualitygate.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("Usuario no encontrado")
            
            if (!firebaseUser.isEmailVerified) {
                auth.signOut()
                throw Exception("Por favor, verifica tu correo electrónico antes de iniciar sesión.")
            }
            
            val uid = firebaseUser.uid
            val userDoc = firestore.collection("users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: throw Exception("Datos de usuario no encontrados")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, pass: String, name: String, role: UserRole): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("Fallo en el registro")
            
            // Enviar correo de verificación
            firebaseUser.sendEmailVerification().await()
            
            val uid = firebaseUser.uid
            val user = User(id = uid, name = name, email = email, role = role)
            firestore.collection("users").document(uid).set(user).await()
            
            // Cerramos sesión después de registrar para que tengan que verificar el correo
            auth.signOut()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
            firestore.collection("users").document(uid).update("name", name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPass: String): Result<Unit> {
        return try {
            val firebaseUser = auth.currentUser ?: throw Exception("Usuario no autenticado")
            firebaseUser.updatePassword(newPass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    suspend fun getUserRole(uid: String): UserRole? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.getString("role")?.let { UserRole.valueOf(it) }
        } catch (e: Exception) {
            null
        }
    }
}
