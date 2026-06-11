package portaria.project.data.repository

import portaria.project.data.api.RepositorioRemoto
import portaria.project.data.models.Encomenda

class EncomendaRepository(private val api: RepositorioRemoto = RepositorioRemoto()) {
    suspend fun getAll(apartmentId: String? = null): Result<List<Encomenda>> = runCatching { api.getEncomendas(apartmentId) }
    suspend fun getById(id: String): Result<Encomenda> = runCatching { api.getEncomendaById(id) }
    suspend fun create(encomenda: Encomenda): Result<String> = runCatching { api.createEncomenda(encomenda) }
    suspend fun update(id: String, encomenda: Encomenda): Result<Unit> = runCatching { api.updateEncomenda(id, encomenda) }
    suspend fun delete(id: String): Result<Unit> = runCatching { api.deleteEncomenda(id) }
}
