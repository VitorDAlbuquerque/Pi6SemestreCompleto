package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.LoginRequest
import portaria.project.data.models.User
import portaria.project.data.repository.AuthRepository

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        println("DEBUG_AUTH: Iniciando login para o e-mail: $email")

        if (email.isBlank() || pass.isBlank()) {
            println("DEBUG_AUTH: Erro de validação - Campos vazios")
            _uiState.value = _uiState.value.copy(error = "Preencha todos os campos obrigatórios.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val request = LoginRequest(email, pass)

            try {
                repository.login(request)
                    .onSuccess { response ->
                        println("DEBUG_AUTH: Sucesso na requisição! Role recebida: ${response.role}")

                        var resolvedId = response.apartmentId
                        var resolvedBlock = response.block
                        val role = response.role.uppercase().trim()
                        if (role == "MORADOR" && !resolvedId.isNullOrBlank()) {
                            runCatching {
                                portaria.project.data.repository.ApartmentRepository().getAll().getOrNull()
                            }.getOrNull()?.let { list ->
                                val apt = list.find { it.id == resolvedId }
                                    ?: list.find { it.number.equals(resolvedId, ignoreCase = true) }
                                if (apt != null) {
                                    resolvedId = apt.id
                                    resolvedBlock = apt.block
                                }
                            }
                        }

                        val loggedUser = User(
                            id = response.userId,
                            name = response.name,
                            email = response.email,
                            role = response.role,
                            apartmentId = resolvedId,
                            block = resolvedBlock
                        )

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentUser = loggedUser,
                            error = null
                        )

                        println("DEBUG_AUTH: Chamando callback onSuccess da tela.")
                        onSuccess()
                    }
                    .onFailure { e ->
                        println("DEBUG_AUTH: Falha na requisição! Erro: ${e.message}")

                        val msgAmigavel = when (e.message) {
                            "USUARIO_NAO_ENCONTRADO" -> "E-mail não registado no sistema."
                            "SENHA_INCORRETA" -> "A senha introduzida está incorreta."
                            "CONTA_DESATIVADA" -> "Conta desativada pelo administrador."
                            "ERRO_DE_CONEXAO" -> "Sem ligação ao servidor."
                            else -> "Erro: ${e.message ?: "Desconhecido"}"
                        }
                        _uiState.value = _uiState.value.copy(isLoading = false, error = msgAmigavel)
                    }
            } catch (ex: Exception) {
                println("DEBUG_AUTH: Exceção crítica inesperada: ${ex.message}")
                ex.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Erro ao conectar com servidor.")
            }
        }
    }

    fun updateCurrentUser(user: User) {
        _uiState.value = _uiState.value.copy(currentUser = user)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun logout(onLogout: () -> Unit) {
        println("DEBUG_AUTH: Efetuando logout...")
        _uiState.value = AuthUiState()
        onLogout()
    }
}