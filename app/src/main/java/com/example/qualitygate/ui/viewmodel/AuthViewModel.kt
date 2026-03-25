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

    private val _updateState = MutableStateFlow<Result<Unit>?>(null)
    val updateState: StateFlow<Result<Unit>?> = _updateState

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val uid = repository.getCurrentUserUid()
            if (uid != null) {
                val result = repository.getUserRole(uid) // This is just a role check, ideally we'd fetch full user
                // For simplicity in this demo, we rely on the login to set the currentUser
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
                // After registration, we don't auto-login because of email verification
                _authState.value = Result.success(User(name = name, email = email, role = role))
            } else {
                _authState.value = Result.failure(result.exceptionOrNull() ?: Exception("Registration failed"))
            }
        }
    }

    fun updateProfile(newName: String) {
        viewModelScope.launch {
            val result = repository.updateProfile(newName)
            _updateState.value = result
            if (result.isSuccess) {
                _currentUser.value = _currentUser.value?.copy(name = newName)
            }
        }
    }

    fun updatePassword(newPass: String) {
        viewModelScope.launch {
            _updateState.value = repository.updatePassword(newPass)
        }
    }

    fun clearUpdateState() {
        _updateState.value = null
    }

    fun logout() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        _currentUser.value = null
        _authState.value = null
    }
}
