package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LprResult(
    val plate: String? = null,
    val status: String = "",
    val residentName: String? = null,
    val apartment: String? = null,
    val message: String = ""
)
