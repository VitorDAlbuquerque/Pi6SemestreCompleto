package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.User
import portaria.project.data.repository.UserRepository

data class MoradorFormFields(
    val nome: String = "",
    val email: String = "",
    val senha: String = "",
    val cpf: String = "",
    val telefone: String = "",
    val nascimento: String = "",
    val selectedApartmentId: String? = null,
    val block: String? = null,
    val tipoMorador: String = "PROPRIETARIO",
    val ativo: Boolean = true
)

data class MoradorUiState(
    val moradores: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val form: MoradorFormFields = MoradorFormFields()
)

class MoradorViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(MoradorUiState())
    val uiState: StateFlow<MoradorUiState> = _uiState

    fun loadMoradores() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAll()
                .onSuccess { users ->
                    val moradores = users.filter { it.role == "MORADOR" }
                    _uiState.value = _uiState.value.copy(moradores = moradores, isLoading = false)
                }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "Erro ao carregar moradores") }
        }
    }

    fun setMoradorForm(update: (MoradorFormFields) -> MoradorFormFields) {
        _uiState.value = _uiState.value.copy(form = update(_uiState.value.form))
    }

    fun prepareMoradorForm(id: String) {
        if (id.isBlank()) {
            _uiState.value = _uiState.value.copy(form = MoradorFormFields())
            return
        }
        val morador = _uiState.value.moradores.find { it.id == id }
        if (morador != null) {
            _uiState.value = _uiState.value.copy(
                form = MoradorFormFields(
                    nome = morador.name,
                    email = morador.email,
                    cpf = morador.cpf,
                    telefone = morador.phone,
                    nascimento = morador.birthDate,
                    selectedApartmentId = morador.apartmentId,
                    block = morador.block,
                    tipoMorador = morador.residentType,
                    ativo = morador.isActive
                )
            )
        }
    }

    fun submitMorador(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val f = _uiState.value.form
            val morador = User(
                id = if (id.isNotBlank()) id else null,
                name = f.nome,
                email = f.email,
                password = f.senha,
                cpf = f.cpf,
                phone = f.telefone,
                birthDate = f.nascimento,
                role = "MORADOR",
                residentType = f.tipoMorador,
                isActive = f.ativo,
                apartmentId = f.selectedApartmentId,
                block = f.block
            )

            val result = if (id.isNotBlank()) repository.update(id, morador) else repository.create(morador)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Sucesso")
                loadMoradores()
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Erro ao salvar")
            }
        }
    }
    fun deleteMorador(id: String) {
        viewModelScope.launch { repository.delete(id).onSuccess { loadMoradores() } }
    }

    fun clearMoradorForm() { _uiState.value = _uiState.value.copy(form = MoradorFormFields()) }
    fun clearMoradorSelecionado() {}
    fun carregarMorador(id: String) { prepareMoradorForm(id) }
    fun clearMessages() { _uiState.value = _uiState.value.copy(error = null, successMessage = null) }
}