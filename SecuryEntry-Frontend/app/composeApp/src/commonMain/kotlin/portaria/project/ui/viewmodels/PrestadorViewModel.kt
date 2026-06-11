package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.Prestador
import portaria.project.data.repository.PrestadorRepository

data class PrestadorFormFields(
    val companyName: String = "",
    val employeeName: String = "",
    val cpf: String = "",
    val phone: String = "",
    val serviceType: String = "",
    val allowedStartTime: String = "08:00",
    val allowedEndTime: String = "18:00",
    val notes: String = "",
    val isActive: Boolean = true
)

data class PrestadorUiState(
    val isLoading: Boolean = false,
    val prestadores: List<Prestador> = emptyList(),
    val prestadorSelecionado: Prestador? = null,
    val form: PrestadorFormFields = PrestadorFormFields(),
    val error: String? = null,
    val successMessage: String? = null
)

class PrestadorViewModel(
    private val repository: PrestadorRepository = PrestadorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrestadorUiState())
    val uiState: StateFlow<PrestadorUiState> = _uiState
    private var formLoadJob: Job? = null


    fun setPrestadorForm(transform: (PrestadorFormFields) -> PrestadorFormFields) {
        _uiState.value = _uiState.value.copy(form = transform(_uiState.value.form))
    }

    fun preparePrestadorForm(id: String) {
        if (id.isBlank()) {
            formLoadJob?.cancel()
            _uiState.value = _uiState.value.copy(
                prestadorSelecionado = null,
                form = PrestadorFormFields(),
                error = null,
                successMessage = null
            )
        } else {
            carregarPrestador(id)
        }
    }

    fun loadPrestadores() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAll()
                .onSuccess { list -> _uiState.value = _uiState.value.copy(isLoading = false, prestadores = list) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar prestadores") }
        }
    }

    fun submitPrestador(id: String) {
        val f = _uiState.value.form
        val prestador = Prestador(
            id = id.ifBlank { null },
            companyName = f.companyName,
            employeeName = f.employeeName,
            cpf = f.cpf,
            phone = f.phone,
            serviceType = f.serviceType,
            allowedStartTime = f.allowedStartTime,
            allowedEndTime = f.allowedEndTime,
            notes = f.notes,
            isActive = f.isActive,
            status = if (f.isActive) "ATIVO" else "BLOQUEADO"
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = if (id.isBlank()) repository.create(prestador) else repository.update(id, prestador)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Prestador gravado com sucesso!")
                loadPrestadores()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Falha ao gravar")
            }
        }
    }

    fun deletePrestador(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.delete(id).onSuccess {
                _uiState.value = _uiState.value.copy(successMessage = "Prestador removido.")
                loadPrestadores()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Falha ao remover")
            }
        }
    }

    private fun carregarPrestador(id: String) {
        formLoadJob?.cancel()
        formLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getById(id).onSuccess { p ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    prestadorSelecionado = p,
                    form = PrestadorFormFields(
                        companyName = p.companyName,
                        employeeName = p.employeeName,
                        cpf = p.cpf,
                        phone = p.phone,
                        serviceType = p.serviceType,
                        allowedStartTime = p.allowedStartTime,
                        allowedEndTime = p.allowedEndTime,
                        notes = p.notes,
                        isActive = p.isActive
                    )
                )
            }.onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar dados") }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}