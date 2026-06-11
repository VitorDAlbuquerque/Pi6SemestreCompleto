package portaria.project.data.repository

import portaria.project.data.api.RepositorioRemoto
import portaria.project.data.models.Visitor

class VisitorRepository(private val api: RepositorioRemoto = RepositorioRemoto()) {
    suspend fun getAll(apartmentId: String? = null): Result<List<Visitor>> = runCatching { api.getVisitors(apartmentId) }
    suspend fun getById(id: String): Result<Visitor> = runCatching { api.getVisitorById(id) }
    suspend fun create(visitor: Visitor): Result<String> = runCatching { api.createVisitor(visitor) }
    suspend fun update(id: String, visitor: Visitor): Result<Unit> = runCatching { api.updateVisitor(id, visitor) }
    suspend fun delete(id: String): Result<Unit> = runCatching { api.deleteVisitor(id) }
}
