package com.example.qualitygate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualitygate.data.model.User
import com.example.qualitygate.data.model.UserRole
import com.example.qualitygate.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = MutableStateFlow<Result<User>?>(null)
    val authState: StateFlow<Result<User>?> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val uid = repository.getCurrentUserUid()
            if (uid != null) {
                val role = repository.getUserRole(uid)
                if (role != null) {
                    // We don't have the full User object easily without another fetch, 
                    // but for now, we can at least fetch the doc if needed.
                    // For simplicity, let's just trigger a re-login or fetch doc.
                    // For now, we leave it to the user to login to ensure fresh credentials.
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        
        viewModelScope.launch {
            val result = repository.login(cleanEmail, cleanPass)
            _authState.value = result
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            }
        }
    }

    fun register(email: String, pass: String, name: String, role: UserRole) {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        
        viewModelScope.launch {
            val result = repository.register(cleanEmail, cleanPass, name, role)
            if (result.isSuccess) {
                login(cleanEmail, cleanPass)
            } else {
                _authState.value = Result.failure(result.exceptionOrNull() ?: Exception("Registration failed"))
            }
        }
    }

    fun logout() {
        // Here we should actually sign out from Firebase
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        _currentUser.value = null
        _authState.value = null
    }
}
