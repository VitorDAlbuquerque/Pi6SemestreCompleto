package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Ocorrencia(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val severity: String = "MEDIA",
    val status: String = "ABERTA",
    val reportedBy: String = "",
    val apartmentId: String = "",
    val createdAt: String = "",
    val resolvedAt: String = "",
    val notes: String = ""
)
