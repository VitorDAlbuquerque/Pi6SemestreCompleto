package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.Visitor
import portaria.project.data.models.User
import portaria.project.data.repository.VisitorRepository
import portaria.project.utils.brParaIso
import portaria.project.utils.isoParaBr

data class VisitanteFormFields(
    val nome: String = "",
    val cpf: String = "",
    val telefone: String = "",
    val dataVisita: String = "",
    val autorizadoPor: String = "",
    val notas: String = "",
    val acessoLivre: Boolean = false,
    val ativo: Boolean = true,
    val selectedApartmentId: String? = null
)

data class VisitanteUiState(
    val isLoading: Boolean = false,
    val visitantes: List<Visitor> = emptyList(),
    val visitanteSelecionado: Visitor? = null,
    val form: VisitanteFormFields = VisitanteFormFields(),
    val error: String? = null,
    val successMessage: String? = null
)

class VisitanteViewModel(
    private val repository: VisitorRepository = VisitorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitanteUiState())
    val uiState: StateFlow<VisitanteUiState> = _uiState
    private var formLoadJob: Job? = null

    fun setVisitanteForm(transform: (VisitanteFormFields) -> VisitanteFormFields) {
        _uiState.value = _uiState.value.copy(form = transform(_uiState.value.form))
    }

    fun clearVisitanteForm() {
        _uiState.value = _uiState.value.copy(
            visitanteSelecionado = null,
            form = VisitanteFormFields(),
            error = null,
            successMessage = null
        )
    }

    fun prepareVisitanteForm(visitanteId: String, fixedApartmentId: String? = null) {
        if (visitanteId.isBlank()) {
            formLoadJob?.cancel()
            formLoadJob = null
            _uiState.value = _uiState.value.copy(
                visitanteSelecionado = null,
                form = VisitanteFormFields(
                    selectedApartmentId = fixedApartmentId?.takeIf { it.isNotBlank() }
                ),
                error = null,
                successMessage = null
            )
        } else {
            carregarVisitante(visitanteId)
        }
    }

    fun loadVisitantes(authenticatedUser: User?) {
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
                    _uiState.value = _uiState.value.copy(isLoading = false, visitantes = filteredList)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar visitantes")
                }
        }
    }

    private fun buildVisitorFromForm(visitanteId: String, fixedApartmentId: String?): Visitor {
        val f = _uiState.value.form
        val apartmentId = fixedApartmentId?.takeIf { it.isNotBlank() }
            ?: f.selectedApartmentId?.takeIf { it.isNotBlank() }
            ?: ""
        val ex = _uiState.value.visitanteSelecionado
        return Visitor(
            id = visitanteId.ifBlank { null },
            name = f.nome,
            cpf = f.cpf,
            phone = f.telefone,
            apartmentId = apartmentId,
            visitDate = brParaIso(f.dataVisita),
            authorizedBy = f.autorizadoPor,
            notes = f.notas,
            freeAccess = f.acessoLivre,
            hasVehicle = ex?.hasVehicle ?: false,
            vehicleId = ex?.vehicleId ?: "",
            isActive = f.ativo
        )
    }

    fun submitVisitante(visitanteId: String, fixedApartmentId: String? = null, currentUser: User? = null) {
        val resolvedApartmentId = if (currentUser?.role?.uppercase()?.trim() == "MORADOR") {
            currentUser.apartmentId
        } else {
            fixedApartmentId
        }
        val visitor = buildVisitorFromForm(visitanteId, resolvedApartmentId)
        if (visitanteId.isBlank()) {
            createVisitante(visitor, currentUser)
        } else {
            updateVisitante(visitanteId, visitor.copy(id = visitanteId), currentUser)
        }
    }

    fun createVisitante(visitor: Visitor, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.create(visitor)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Visitante cadastrado com sucesso")
                    loadVisitantes(currentUser)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao cadastrar visitante")
                }
        }
    }

    fun updateVisitante(id: String, visitor: Visitor, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.update(id, visitor)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Visitante atualizado")
                    loadVisitantes(currentUser)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao atualizar visitante")
                }
        }
    }

    fun deleteVisitante(id: String, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.delete(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = "Visitante removido")
                    loadVisitantes(currentUser)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao remover visitante")
                }
        }
    }

    fun carregarVisitante(id: String) {
        formLoadJob?.cancel()
        formLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getById(id)
                .onSuccess { v ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        visitanteSelecionado = v,
                        form = VisitanteFormFields(
                            nome = v.name,
                            cpf = v.cpf,
                            telefone = v.phone,
                            dataVisita = isoParaBr(v.visitDate),
                            autorizadoPor = v.authorizedBy,
                            notas = v.notes,
                            acessoLivre = v.freeAccess,
                            ativo = v.isActive,
                            selectedApartmentId = v.apartmentId.takeIf { it.isNotBlank() }
                        )
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar visitante")
                }
        }
    }

    fun clearVisitanteSelecionado() {
        _uiState.value = _uiState.value.copy(visitanteSelecionado = null)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}