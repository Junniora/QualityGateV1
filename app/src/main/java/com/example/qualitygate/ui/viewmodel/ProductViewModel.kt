package com.example.qualitygate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualitygate.data.model.*
import com.example.qualitygate.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository = ProductRepository()) : ViewModel() {

    private val _registrationState = MutableStateFlow<Result<String>?>(null)
    val registrationState: StateFlow<Result<String>?> = _registrationState

    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _productList

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

    fun registerProduct(
        classification: ProductClassification,
        partNumber: String,
        supervisorName: String
    ) {
        viewModelScope.launch {
            val product = Product(
                classification = classification,
                partNumber = partNumber,
                supervisorName = supervisorName,
                status = ProductStatus.PLANNING
            )
            val result = repository.registerProduct(product)
            _registrationState.value = result
            if (result.isSuccess) {
                fetchProducts()
            }
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
