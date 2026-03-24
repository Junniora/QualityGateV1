package com.example.qualitygate.data.model

import com.google.firebase.Timestamp

enum class UserRole {
    SUPERVISOR, REVISOR, APROBADOR
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.SUPERVISOR
)

enum class ProductStatus {
    PLANNING, PRE_REVISION, ON_GOING, FINAL_REVISION, APROBACION_FINAL, COMPLETED, RECHAZADO
}

enum class ProductClassification {
    NUEVO_PRODUCTO, TRANSFERENCIA_PRODUCTO
}

data class Product(
    val id: String = "",
    val classification: ProductClassification = ProductClassification.NUEVO_PRODUCTO,
    val supervisorName: String = "",
    val partNumber: String = "",
    val registrationDate: Timestamp = Timestamp.now(),
    val productionStartDate: Timestamp = Timestamp.now(),
    val photos: List<String> = emptyList(),
    val status: ProductStatus = ProductStatus.PLANNING
)

enum class MilestoneStatus {
    PENDIENTE, EN_REVISION, ON_GOING, COMPLETADO, RECHAZADO
}

data class Milestone(
    val id: String = "",
    val productId: String = "",
    val name: String = "",
    val order: Int = 0,
    val plannedStart: Timestamp? = null,
    val plannedEnd: Timestamp? = null,
    val realStart: Timestamp? = null,
    val realEnd: Timestamp? = null,
    val status: MilestoneStatus = MilestoneStatus.PENDIENTE
)

data class Feedback(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val comment: String = "",
    val date: Timestamp = Timestamp.now()
)
