package portaria.project.data.repository

import portaria.project.data.api.RepositorioRemoto
import portaria.project.data.models.Prestador

class PrestadorRepository(private val api: RepositorioRemoto = RepositorioRemoto()) {

    suspend fun getAll(): Result<List<Prestador>> = runCatching {
        api.getPrestadores()
    }

    suspend fun getById(id: String): Result<Prestador> = runCatching {
        api.getPrestadorById(id)
    }

    suspend fun create(prestador: Prestador): Result<Prestador> = runCatching {
        val generatedId = api.createPrestador(prestador)
        prestador.copy(id = generatedId)
    }

    suspend fun update(id: String, prestador: Prestador): Result<Prestador> = runCatching {
        api.updatePrestador(id, prestador)
        prestador
    }

    suspend fun delete(id: String): Result<Unit> = runCatching {
        api.deletePrestador(id)
    }
}