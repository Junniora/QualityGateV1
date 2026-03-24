package com.example.qualitygate.data.repository

import com.example.qualitygate.data.model.*
import com.example.qualitygate.data.util.MilestoneTemplates
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerProduct(product: Product): Result<String> {
        return try {
            val productRef = firestore.collection("products").document()
            val newId = productRef.id
            val newProduct = product.copy(id = newId)
            
            // Usamos un batch para asegurar que el producto y sus milestones se crean juntos
            val batch = firestore.batch()
            batch.set(productRef, newProduct)

            val templateNames = MilestoneTemplates.getMilestonesFor(newProduct.classification)
            templateNames.forEachIndexed { index, name ->
                val milestoneRef = firestore.collection("milestones").document()
                val milestone = Milestone(
                    id = milestoneRef.id,
                    productId = newId,
                    name = name,
                    order = index,
                    status = MilestoneStatus.PENDIENTE
                )
                batch.set(milestoneRef, milestone)
            }
            
            batch.commit().await()
            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .orderBy("registrationDate", Query.Direction.DESCENDING)
                .get().await()
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMilestones(productId: String): Result<List<Milestone>> {
        return try {
            val snapshot = firestore.collection("milestones")
                .whereEqualTo("productId", productId)
                .get().await()
            
            val milestones = snapshot.toObjects(Milestone::class.java)
                .sortedBy { it.order }

            Result.success(milestones)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProductStatus(productId: String, newStatus: ProductStatus): Result<Unit> {
        return try {
            firestore.collection("products").document(productId)
                .update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMilestone(milestone: Milestone): Result<Unit> {
        return try {
            firestore.collection("milestones").document(milestone.id).set(milestone).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
