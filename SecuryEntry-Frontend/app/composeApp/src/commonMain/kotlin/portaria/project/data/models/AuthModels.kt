package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val message: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "MORADOR",
    val apartmentId: String? = null,
    val block: String? = null
)

@Serializable
data class ErrorResponse(
    val message: String = "Ocorreu um erro desconhecido"
)