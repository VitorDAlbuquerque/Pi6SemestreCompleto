package portaria.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RegistroAcesso(
    val id: String? = null,
    val tipoEvento: String = "ENTRADA",
    val pessoaNome: String = "",
    val pessoaTipo: String = "MORADOR",
    val veiculoPlaca: String = "",
    val dataHora: String = "",
    val status: String = "AUTORIZADO",
    val observacao: String = ""
)