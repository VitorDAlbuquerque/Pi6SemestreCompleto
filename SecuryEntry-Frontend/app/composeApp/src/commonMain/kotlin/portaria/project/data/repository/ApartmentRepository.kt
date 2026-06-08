package portaria.project.data.repository

import portaria.project.data.api.RepositorioRemoto
import portaria.project.data.models.Apartment

class ApartmentRepository(private val api: RepositorioRemoto = RepositorioRemoto()) {
    suspend fun getAll(): Result<List<Apartment>> = runCatching { api.getApartments() }
    suspend fun getById(id: String): Result<Apartment> = runCatching { api.getApartmentById(id) }
    suspend fun create(apartment: Apartment): Result<String> = runCatching { api.createApartment(apartment) }
    suspend fun update(id: String, apartment: Apartment): Result<Unit> = runCatching { api.updateApartment(id, apartment) }
    suspend fun delete(id: String): Result<Unit> = runCatching { api.deleteApartment(id) }
}
