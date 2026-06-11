package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Apartment(
    val id: String? = null,
    val number: String = "",
    val block: String = "",
    val floor: String = "",
    val residentIds: List<String> = emptyList(),
    val parkingSpotCount: Int = 0,
    val availableParkingSpots: Int = 0,
    val notes: String = "",
    val isActive: Boolean = true
)