package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ReservaVaga(
    val id: String? = null,
    val apartmentId: String = "",
    val residentName: String = "",
    val spotNumber: String = "",
    val vehiclePlate: String = "",
    val startDateTime: String = "",
    val endDateTime: String = "",
    val status: String = "PENDENTE",
    val notes: String = ""
)
