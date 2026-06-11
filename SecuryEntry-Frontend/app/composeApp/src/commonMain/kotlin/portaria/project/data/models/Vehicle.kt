package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Vehicle(
    val id: String? = null,
    val plate: String = "",
    val brand: String = "",
    val model: String = "",
    val color: String = "",
    val year: Int? = null,
    val type: String = "",
    val apartmentId: String = "",
    val visitorId: String = "",
    val notes: String = "",
    val isActive: Boolean = true
)
