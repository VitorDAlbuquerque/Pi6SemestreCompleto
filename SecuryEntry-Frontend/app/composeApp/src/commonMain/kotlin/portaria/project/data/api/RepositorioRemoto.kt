package portaria.project.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import portaria.project.data.models.*

class RepositorioRemoto(private val client: HttpClient = provideHttpClient()) {

    private val base = ApiConfig.BASE_URL
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private suspend fun erroAmigavel(e: ResponseException): Exception {
        val body = runCatching { e.response.bodyAsText() }.getOrDefault("")
        val msg = runCatching { json.decodeFromString<ErrorResponse>(body).message }
            .getOrNull()
        return Exception(msg ?: when (e.response.status.value) {
            400 -> "Dados inválidos. Verifique as informações e tente novamente."
            401 -> "Credenciais inválidas."
            403 -> "Acesso negado."
            404 -> "Registro não encontrado."
            409 -> "Esse registro já existe."
            500 -> "Algo deu errado no servidor. Tente novamente em instantes."
            else -> "Algo deu errado. Tente novamente em instantes."
        })
    }

    private suspend inline fun <T> call(block: () -> T): T {
        try {
            return block()
        } catch (e: ResponseException) {
            throw erroAmigavel(e)
        }
    }

    suspend fun login(request: LoginRequest): LoginResponse = call {
        client.post("$base/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun createUser(user: User): String = call {
        client.post("$base/users") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }

    suspend fun getUsers(): List<User> = call {
        client.get("$base/users").body()
    }

    suspend fun getUserById(id: String): User = call {
        client.get("$base/users/$id").body()
    }

    suspend fun updateUser(id: String, user: User) = call {
        client.put("$base/users/$id") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.bodyAsText()
    }

    suspend fun deleteUser(id: String) = call {
        client.delete("$base/users/$id").bodyAsText()
    }

    suspend fun createApartment(apartment: Apartment): String = call {
        client.post("$base/apartments") {
            contentType(ContentType.Application.Json)
            setBody(apartment)
        }.body()
    }

    suspend fun getApartments(): List<Apartment> = call {
        client.get("$base/apartments").body()
    }

    suspend fun getApartmentById(id: String): Apartment = call {
        client.get("$base/apartments/$id").body()
    }

    suspend fun updateApartment(id: String, apartment: Apartment) = call {
        client.put("$base/apartments/$id") {
            contentType(ContentType.Application.Json)
            setBody(apartment)
        }.bodyAsText()
    }

    suspend fun deleteApartment(id: String) = call {
        client.delete("$base/apartments/$id").bodyAsText()
    }

    suspend fun createVehicle(vehicle: Vehicle): String = call {
        client.post("$base/vehicles") {
            contentType(ContentType.Application.Json)
            setBody(vehicle)
        }.body()
    }

    suspend fun getVehicles(apartmentId: String? = null): List<Vehicle> = call {
        client.get("$base/vehicles") {
            if (!apartmentId.isNullOrBlank()) {
                parameter("apartmentId", apartmentId)
            }
        }.body()
    }

    suspend fun getVehicleById(id: String): Vehicle = call {
        client.get("$base/vehicles/$id").body()
    }

    suspend fun updateVehicle(id: String, vehicle: Vehicle) = call {
        client.put("$base/vehicles/$id") {
            contentType(ContentType.Application.Json)
            setBody(vehicle)
        }.bodyAsText()
    }

    suspend fun deleteVehicle(id: String) = call {
        client.delete("$base/vehicles/$id").bodyAsText()
    }

    suspend fun createVisitor(visitor: Visitor): String = call {
        client.post("$base/visitors") {
            contentType(ContentType.Application.Json)
            setBody(visitor)
        }.body()
    }

    suspend fun getVisitors(apartmentId: String? = null): List<Visitor> = call {
        client.get("$base/visitors") {
            if (!apartmentId.isNullOrBlank()) {
                parameter("apartmentId", apartmentId)
            }
        }.body()
    }

    suspend fun getVisitorById(id: String): Visitor = call {
        client.get("$base/visitors/$id").body()
    }

    suspend fun updateVisitor(id: String, visitor: Visitor) = call {
        client.put("$base/visitors/$id") {
            contentType(ContentType.Application.Json)
            setBody(visitor)
        }.bodyAsText()
    }

    suspend fun deleteVisitor(id: String) = call {
        client.delete("$base/visitors/$id").bodyAsText()
    }

    suspend fun createEncomenda(encomenda: Encomenda): String = call {
        client.post("$base/encomendas") {
            contentType(ContentType.Application.Json)
            setBody(encomenda)
        }.body()
    }

    suspend fun getEncomendas(apartmentId: String? = null): List<Encomenda> = call {
        client.get("$base/encomendas") {
            if (!apartmentId.isNullOrBlank()) {
                parameter("apartmentId", apartmentId)
            }
        }.body()
    }

    suspend fun getEncomendaById(id: String): Encomenda = call {
        client.get("$base/encomendas/$id").body()
    }

    suspend fun updateEncomenda(id: String, encomenda: Encomenda) = call {
        client.put("$base/encomendas/$id") {
            contentType(ContentType.Application.Json)
            setBody(encomenda)
        }.bodyAsText()
    }

    suspend fun deleteEncomenda(id: String) = call {
        client.delete("$base/encomendas/$id").bodyAsText()
    }

    suspend fun createPrestador(prestador: Prestador): String = call {
        client.post("$base/prestadores") {
            contentType(ContentType.Application.Json)
            setBody(prestador)
        }.body()
    }

    suspend fun getPrestadores(): List<Prestador> = call {
        client.get("$base/prestadores").body()
    }

    suspend fun getPrestadorById(id: String): Prestador = call {
        client.get("$base/prestadores/$id").body()
    }

    suspend fun updatePrestador(id: String, prestador: Prestador) = call {
        client.put("$base/prestadores/$id") {
            contentType(ContentType.Application.Json)
            setBody(prestador)
        }.bodyAsText()
    }

    suspend fun deletePrestador(id: String) = call {
        client.delete("$base/prestadores/$id").bodyAsText()
    }

    suspend fun triggerLpr(): portaria.project.data.models.LprResult = call {
        client.post("$base/lpr/trigger").body()
    }

    suspend fun abrirPortao() = call {
        client.post("$base/servo/abrir").bodyAsText()
    }
}