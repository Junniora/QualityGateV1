package com.example.qualitygate.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualitygate.data.model.*
import com.example.qualitygate.data.repository.ProductRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository = ProductRepository()) : ViewModel() {

    private val _registrationState = MutableStateFlow<Result<String>?>(null)
    val registrationState: StateFlow<Result<String>?> = _registrationState

    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _productList

    private val _productFeedback = MutableStateFlow<Map<String, List<Feedback>>>(emptyMap())
    val productFeedback: StateFlow<Map<String, List<Feedback>>> = _productFeedback

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            val result = repository.getProducts()
            if (result.isSuccess) {
                _productList.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    fun fetchMilestones(productId: String, onResult: (List<Milestone>) -> Unit) {
        viewModelScope.launch {
            val result = repository.getMilestones(productId)
            if (result.isSuccess) {
                onResult(result.getOrNull() ?: emptyList())
            } else {
                onResult(emptyList())
            }
        }
    }

    fun fetchFeedback(productId: String) {
        viewModelScope.launch {
            val result = repository.getFeedback(productId)
            if (result.isSuccess) {
                val currentMap = _productFeedback.value.toMutableMap()
                currentMap[productId] = result.getOrNull() ?: emptyList()
                _productFeedback.value = currentMap
            }
        }
    }

    fun addFeedback(productId: String, userId: String, comment: String) {
        viewModelScope.launch {
            val feedback = Feedback(productId = productId, userId = userId, comment = comment, date = Timestamp.now())
            val result = repository.addFeedback(feedback)
            if (result.isSuccess) {
                fetchFeedback(productId)
            }
        }
    }

    fun registerProduct(
        classification: ProductClassification,
        partNumber: String,
        serialNumber: String,
        description: String,
        provider: String,
        supervisorName: String,
        supervisorId: String,
        photoUris: List<Uri>
    ) {
        viewModelScope.launch {
            val product = Product(
                classification = classification,
                partNumber = partNumber,
                serialNumber = serialNumber,
                description = description,
                provider = provider,
                supervisorName = supervisorName,
                supervisorId = supervisorId,
                status = ProductStatus.PLANNING,
                registrationDate = Timestamp.now()
            )
            val result = repository.registerProduct(product, photoUris)
            _registrationState.value = result
            if (result.isSuccess) {
                fetchProducts()
            }
        }
    }

    fun updateProduct(product: Product, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateProduct(product)
            if (result.isSuccess) fetchProducts()
            onComplete(result.isSuccess)
        }
    }

    fun deleteProduct(productId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteProduct(productId)
            if (result.isSuccess) fetchProducts()
            onComplete(result.isSuccess)
        }
    }

    fun updateMilestone(milestone: Milestone, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateMilestone(milestone)
            onComplete(result.isSuccess)
        }
    }

    fun updateProductStatus(productId: String, newStatus: ProductStatus, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateProductStatus(productId, newStatus)
            if (result.isSuccess) {
                fetchProducts()
            }
            onComplete(result.isSuccess)
        }
    }
}
