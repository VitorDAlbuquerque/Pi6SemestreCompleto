package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.ReservaVaga
import portaria.project.data.models.User
import portaria.project.data.repository.ReservaVagaRepository

data class ReservaVagaFormFields(
    val apartmentId: String = "",
    val residentName: String = "",
    val spotNumber: String = "",
    val vehiclePlate: String = "",
    val startDateTime: String = "",
    val endDateTime: String = "",
    val status: String = "PENDENTE",
    val notes: String = ""
)

data class ReservaVagaUiState(
    val isLoading: Boolean = false,
    val reservas: List<ReservaVaga> = emptyList(),
    val reservaSelecionada: ReservaVaga? = null,
    val form: ReservaVagaFormFields = ReservaVagaFormFields(),
    val error: String? = null,
    val successMessage: String? = null
)

class ReservaVagaViewModel(
    private val repository: ReservaVagaRepository = ReservaVagaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservaVagaUiState())
    val uiState: StateFlow<ReservaVagaUiState> = _uiState

    fun setReservaForm(transform: (ReservaVagaFormFields) -> ReservaVagaFormFields) {
        _uiState.value = _uiState.value.copy(form = transform(_uiState.value.form))
    }

    fun prepareReservaForm(id: String, apartmentId: String? = null) {
        if (id.isBlank()) {
            _uiState.value = _uiState.value.copy(
                reservaSelecionada = null,
                form = ReservaVagaFormFields(apartmentId = apartmentId ?: ""),
                error = null,
                successMessage = null
            )
        } else {
            carregarReserva(id)
        }
    }

    fun loadReservas(authenticatedUser: User?) {
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
                    _uiState.value = _uiState.value.copy(isLoading = false, reservas = filteredList)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar reservas")
                }
        }
    }

    fun submitReserva(id: String, currentUser: User? = null) {
        val f = _uiState.value.form
        val reserva = ReservaVaga(
            id = id.ifBlank { null },
            apartmentId = f.apartmentId,
            residentName = f.residentName,
            spotNumber = f.spotNumber,
            vehiclePlate = f.vehiclePlate,
            startDateTime = f.startDateTime,
            endDateTime = f.endDateTime,
            status = f.status,
            notes = f.notes
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = if (id.isBlank()) repository.create(reserva) else repository.update(id, reserva)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Reserva gravada com sucesso")
                loadReservas(currentUser)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao gravar reserva")
            }
        }
    }

    fun deleteReserva(id: String, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.delete(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = "Reserva removida")
                    loadReservas(currentUser)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao remover reserva")
                }
        }
    }

    private fun carregarReserva(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getById(id)
                .onSuccess { r ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reservaSelecionada = r,
                        form = ReservaVagaFormFields(
                            apartmentId = r.apartmentId,
                            residentName = r.residentName,
                            spotNumber = r.spotNumber,
                            vehiclePlate = r.vehiclePlate,
                            startDateTime = r.startDateTime,
                            endDateTime = r.endDateTime,
                            status = r.status,
                            notes = r.notes
                        )
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar reserva")
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
