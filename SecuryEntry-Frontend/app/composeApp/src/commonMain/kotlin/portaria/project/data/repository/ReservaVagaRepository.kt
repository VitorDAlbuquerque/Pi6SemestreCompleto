package portaria.project.data.repository

import portaria.project.data.api.RepositorioRemoto
import portaria.project.data.models.ReservaVaga

class ReservaVagaRepository(private val api: RepositorioRemoto = RepositorioRemoto()) {
    suspend fun getAll(apartmentId: String? = null): Result<List<ReservaVaga>> = runCatching { api.getReservasVagas(apartmentId) }
    suspend fun getById(id: String): Result<ReservaVaga> = runCatching { api.getReservaVagaById(id) }
    suspend fun create(reserva: ReservaVaga): Result<String> = runCatching { api.createReservaVaga(reserva) }
    suspend fun update(id: String, reserva: ReservaVaga): Result<Unit> = runCatching { api.updateReservaVaga(id, reserva) }
    suspend fun delete(id: String): Result<Unit> = runCatching { api.deleteReservaVaga(id) }
}
