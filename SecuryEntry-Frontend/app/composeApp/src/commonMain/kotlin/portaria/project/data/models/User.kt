package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val birthDate: String = "",
    val cpf: String = "",
    val role: String = "MORADOR",
    val residentType: String = "PROPRIETARIO",
    val isActive: Boolean = true,
    val apartmentId: String? = null,
    val block: String? = null
)