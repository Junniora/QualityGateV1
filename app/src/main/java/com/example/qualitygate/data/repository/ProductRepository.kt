package com.example.qualitygate.data.repository

import android.net.Uri
import com.example.qualitygate.data.model.*
import com.example.qualitygate.data.util.MilestoneTemplates
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadPhotos(photoUris: List<Uri>): List<String> {
        val downloadUrls = mutableListOf<String>()
        for (uri in photoUris) {
            val fileName = "products/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)
            storageRef.putFile(uri).await()
            val url = storageRef.downloadUrl.await().toString()
            downloadUrls.add(url)
        }
        return downloadUrls
    }

    suspend fun registerProduct(product: Product, photoUris: List<Uri>): Result<String> {
        return try {
            val imageUrls = if (photoUris.isNotEmpty()) uploadPhotos(photoUris) else emptyList()
            val productRef = firestore.collection("products").document()
            val newId = productRef.id
            val newProduct = product.copy(
                id = newId,
                photos = imageUrls
            )
            
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

    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            firestore.collection("products").document(product.id).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            batch.delete(firestore.collection("products").document(productId))
            
            // También deberíamos borrar hitos y feedback asociados
            val milestones = firestore.collection("milestones").whereEqualTo("productId", productId).get().await()
            milestones.documents.forEach { batch.delete(it.reference) }
            
            val feedback = firestore.collection("feedback").whereEqualTo("productId", productId).get().await()
            feedback.documents.forEach { batch.delete(it.reference) }

            batch.commit().await()
            Result.success(Unit)
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

    suspend fun addFeedback(feedback: Feedback): Result<Unit> {
        return try {
            val ref = firestore.collection("feedback").document()
            firestore.collection("feedback").document(ref.id).set(feedback.copy(id = ref.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFeedback(productId: String): Result<List<Feedback>> {
        return try {
            val snapshot = firestore.collection("feedback")
                .whereEqualTo("productId", productId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Feedback::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
