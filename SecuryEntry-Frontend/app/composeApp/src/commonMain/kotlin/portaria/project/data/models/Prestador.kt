package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Prestador(
    val id: String? = null,
    val companyName: String = "",
    val employeeName: String = "",
    val cpf: String = "",
    val phone: String = "",
    val serviceType: String = "",
    val allowedStartTime: String = "08:00",
    val allowedEndTime: String = "18:00",
    val status: String = "ATIVO",
    val notes: String = "",
    val isActive: Boolean = true
)