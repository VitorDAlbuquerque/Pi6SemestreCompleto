package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.User
import portaria.project.data.repository.UserRepository

data class UsuarioFormFields(
    val name: String = "",
    val email: String = "",
    val role: String = "PORTEIRO",
    val isActive: Boolean = true
)

data class UsuarioUiState(
    val isLoading: Boolean = false,
    val usuarios: List<User> = emptyList(),
    val form: UsuarioFormFields = UsuarioFormFields(),
    val error: String? = null,
    val successMessage: String? = null
)

class UsuarioViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuarioUiState())
    val uiState: StateFlow<UsuarioUiState> = _uiState
    private var formLoadJob: Job? = null


    fun loadUsuarios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAll()
                .onSuccess { list ->
                    // Filtra apenas utilizadores da portaria (ignora moradores)
                    val portariaUsers = list.filter { it.role == "ADMIN" || it.role == "PORTEIRO" || it.role == "GERENTE" }
                    _uiState.value = _uiState.value.copy(isLoading = false, usuarios = portariaUsers)
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar usuários") }
        }
    }

    fun setUsuarioForm(transform: (UsuarioFormFields) -> UsuarioFormFields) {
        _uiState.value = _uiState.value.copy(form = transform(_uiState.value.form))
    }

    fun prepareUsuarioForm(id: String) {
        if (id.isBlank()) {
            formLoadJob?.cancel()
            _uiState.value = _uiState.value.copy(form = UsuarioFormFields(), error = null, successMessage = null)
        } else {
            carregarUsuario(id)
        }
    }

    private fun carregarUsuario(id: String) {
        formLoadJob?.cancel()
        formLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getById(id).onSuccess { u ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    form = UsuarioFormFields(
                        name = u.name,
                        email = u.email,
                        role = u.role.ifBlank { "PORTEIRO" },
                        isActive = u.isActive
                    )
                )
            }.onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Falha ao carregar usuário") }
        }
    }

    fun submitUsuario(id: String) {
        val f = _uiState.value.form
        val user = User(
            id = id.ifBlank { null },
            name = f.name,
            email = f.email,
            password = "",
            role = f.role,
            isActive = f.isActive
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = if (id.isBlank()) repository.create(user) else repository.update(id, user)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Utilizador gravado com sucesso!")
                loadUsuarios()
            }.onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao salvar utilizador") }
        }
    }

    fun deleteUsuario(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.delete(id).onSuccess {
                _uiState.value = _uiState.value.copy(successMessage = "Utilizador removido.")
                loadUsuarios()
            }.onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao remover") }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}