package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Visitor(
    val id: String? = null,
    val name: String = "",
    val cpf: String = "",
    val phone: String = "",
    val apartmentId: String = "",
    val visitDate: String = "",
    val freeAccess: Boolean = false,
    val hasVehicle: Boolean = false,
    val vehicleId: String = "",
    val authorizedBy: String = "",
    val notes: String = "",
    val isActive: Boolean = true
)
