package portaria.project.data.repository

import portaria.project.data.api.RepositorioRemoto
import portaria.project.data.models.Vehicle

class VehicleRepository(private val api: RepositorioRemoto = RepositorioRemoto()) {
    suspend fun getAll(apartmentId: String? = null): Result<List<Vehicle>> = runCatching { api.getVehicles(apartmentId) }
    suspend fun getById(id: String): Result<Vehicle> = runCatching { api.getVehicleById(id) }
    suspend fun create(vehicle: Vehicle): Result<String> = runCatching { api.createVehicle(vehicle) }
    suspend fun update(id: String, vehicle: Vehicle): Result<Unit> = runCatching { api.updateVehicle(id, vehicle) }
    suspend fun delete(id: String): Result<Unit> = runCatching { api.deleteVehicle(id) }
}
