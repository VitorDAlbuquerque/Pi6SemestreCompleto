package portaria.project.data.repository

import portaria.project.data.api.RepositorioRemoto
import portaria.project.data.models.Ocorrencia

class OcorrenciaRepository(private val api: RepositorioRemoto = RepositorioRemoto()) {
    suspend fun getAll(apartmentId: String? = null): Result<List<Ocorrencia>> = runCatching { api.getOcorrencias(apartmentId) }
    suspend fun getById(id: String): Result<Ocorrencia> = runCatching { api.getOcorrenciaById(id) }
    suspend fun create(ocorrencia: Ocorrencia): Result<String> = runCatching { api.createOcorrencia(ocorrencia) }
    suspend fun update(id: String, ocorrencia: Ocorrencia): Result<Unit> = runCatching { api.updateOcorrencia(id, ocorrencia) }
    suspend fun delete(id: String): Result<Unit> = runCatching { api.deleteOcorrencia(id) }
}
