package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.Ocorrencia
import portaria.project.data.models.User
import portaria.project.data.repository.OcorrenciaRepository

data class OcorrenciaFormFields(
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

data class OcorrenciaUiState(
    val isLoading: Boolean = false,
    val ocorrencias: List<Ocorrencia> = emptyList(),
    val ocorrenciaSelecionada: Ocorrencia? = null,
    val form: OcorrenciaFormFields = OcorrenciaFormFields(),
    val error: String? = null,
    val successMessage: String? = null
)

class OcorrenciaViewModel(
    private val repository: OcorrenciaRepository = OcorrenciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcorrenciaUiState())
    val uiState: StateFlow<OcorrenciaUiState> = _uiState

    fun setOcorrenciaForm(transform: (OcorrenciaFormFields) -> OcorrenciaFormFields) {
        _uiState.value = _uiState.value.copy(form = transform(_uiState.value.form))
    }

    fun prepareOcorrenciaForm(id: String, currentUser: User? = null) {
        if (id.isBlank()) {
            _uiState.value = _uiState.value.copy(
                ocorrenciaSelecionada = null,
                form = OcorrenciaFormFields(
                    reportedBy = currentUser?.name ?: "",
                    apartmentId = if (currentUser?.role?.uppercase()?.trim() == "MORADOR") currentUser.apartmentId ?: "" else ""
                ),
                error = null,
                successMessage = null
            )
        } else {
            carregarOcorrencia(id)
        }
    }

    fun loadOcorrencias(authenticatedUser: User?) {
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
                    _uiState.value = _uiState.value.copy(isLoading = false, ocorrencias = filteredList)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar ocorrencias")
                }
        }
    }

    fun submitOcorrencia(id: String, currentUser: User? = null) {
        val f = _uiState.value.form
        val ocorrencia = Ocorrencia(
            id = id.ifBlank { null },
            title = f.title,
            description = f.description,
            category = f.category,
            severity = f.severity,
            status = f.status,
            reportedBy = f.reportedBy,
            apartmentId = f.apartmentId,
            createdAt = f.createdAt,
            resolvedAt = f.resolvedAt,
            notes = f.notes
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = if (id.isBlank()) repository.create(ocorrencia) else repository.update(id, ocorrencia)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Ocorrencia gravada com sucesso")
                loadOcorrencias(currentUser)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao gravar ocorrencia")
            }
        }
    }

    fun deleteOcorrencia(id: String, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.delete(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = "Ocorrencia removida")
                    loadOcorrencias(currentUser)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao remover ocorrencia")
                }
        }
    }

    private fun carregarOcorrencia(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getById(id)
                .onSuccess { o ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        ocorrenciaSelecionada = o,
                        form = OcorrenciaFormFields(
                            title = o.title,
                            description = o.description,
                            category = o.category,
                            severity = o.severity,
                            status = o.status,
                            reportedBy = o.reportedBy,
                            apartmentId = o.apartmentId,
                            createdAt = o.createdAt,
                            resolvedAt = o.resolvedAt,
                            notes = o.notes
                        )
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar ocorrencia")
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
