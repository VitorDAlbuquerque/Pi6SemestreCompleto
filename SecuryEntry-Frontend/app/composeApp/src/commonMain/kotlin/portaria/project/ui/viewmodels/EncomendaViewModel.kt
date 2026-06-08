package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.Encomenda
import portaria.project.data.models.User
import portaria.project.data.repository.EncomendaRepository

data class EncomendaFormFields(
    val description: String = "",
    val recipientName: String = "",
    val storageLocation: String = "",
    val apartmentId: String = "",
    val deliveredToResident: Boolean = false,
    val notes: String = ""
)

data class EncomendaUiState(
    val isLoading: Boolean = false,
    val encomendas: List<Encomenda> = emptyList(),
    val encomendaSelecionada: Encomenda? = null,
    val form: EncomendaFormFields = EncomendaFormFields(),
    val error: String? = null,
    val successMessage: String? = null
)

class EncomendaViewModel(
    private val repository: EncomendaRepository = EncomendaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EncomendaUiState())
    val uiState: StateFlow<EncomendaUiState> = _uiState

    fun setEncomendaForm(transform: (EncomendaFormFields) -> EncomendaFormFields) {
        _uiState.value = _uiState.value.copy(form = transform(_uiState.value.form))
    }

    fun clearEncomendaForm() {
        _uiState.value = _uiState.value.copy(
            encomendaSelecionada = null,
            form = EncomendaFormFields(),
            error = null,
            successMessage = null
        )
    }

    fun prepareEncomendaForm(encomendaId: String) {
        if (encomendaId.isBlank()) {
            clearEncomendaForm()
        } else {
            carregarEncomenda(encomendaId)
        }
    }

    fun loadEncomendas(authenticatedUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val role = authenticatedUser?.role?.uppercase()?.trim()
            val apartmentId = if (role == "MORADOR") authenticatedUser.apartmentId else null

            repository.getAll(apartmentId)
                .onSuccess { list ->
                    val filteredList = when (role) {
                        "MORADOR" -> list.filter { it.apartmentId == authenticatedUser.apartmentId }
                        "ADMIN", "PORTEIRO", "GERENTE" -> list
                        else -> emptyList()
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, encomendas = filteredList)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar encomendas")
                }
        }
    }

    private fun buildEncomendaFromForm(encomendaId: String): Encomenda {
        val f = _uiState.value.form
        return Encomenda(
            id = encomendaId.ifBlank { null },
            description = f.description,
            recipientName = f.recipientName,
            storageLocation = f.storageLocation,
            apartmentId = f.apartmentId,
            deliveredToResident = f.deliveredToResident,
            notes = f.notes
        )
    }

    fun submitEncomenda(encomendaId: String, currentUser: User? = null) {
        val e = buildEncomendaFromForm(encomendaId)
        if (encomendaId.isBlank()) {
            createEncomenda(e, currentUser)
        } else {
            updateEncomenda(encomendaId, e.copy(id = encomendaId), currentUser)
        }
    }

    fun createEncomenda(encomenda: Encomenda, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.create(encomenda)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Encomenda registrada")
                    loadEncomendas(currentUser)
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message ?: "Erro ao criar encomenda")
                }
        }
    }

    fun updateEncomenda(id: String, encomenda: Encomenda, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.update(id, encomenda)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Encomenda atualizada")
                    loadEncomendas(currentUser)
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message ?: "Erro ao atualizar encomenda")
                }
        }
    }

    fun deleteEncomenda(id: String, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.delete(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = "Encomenda removida")
                    loadEncomendas(currentUser)
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message ?: "Erro ao remover encomenda")
                }
        }
    }

    fun carregarEncomenda(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getById(id)
                .onSuccess { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        encomendaSelecionada = e,
                        form = EncomendaFormFields(
                            description = e.description,
                            recipientName = e.recipientName,
                            storageLocation = e.storageLocation,
                            apartmentId = e.apartmentId,
                            deliveredToResident = e.deliveredToResident,
                            notes = e.notes
                        )
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message ?: "Erro ao carregar encomenda")
                }
        }
    }

    fun clearEncomendaSelecionada() {
        _uiState.value = _uiState.value.copy(encomendaSelecionada = null)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}