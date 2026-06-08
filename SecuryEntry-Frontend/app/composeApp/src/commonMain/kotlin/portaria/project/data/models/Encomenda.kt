package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Encomenda(
    val id: String? = null,
    val description: String = "",
    val recipientName: String = "",
    val storageLocation: String = "",
    val apartmentId: String = "",
    val deliveredToResident: Boolean = false,
    val notes: String = ""
)
