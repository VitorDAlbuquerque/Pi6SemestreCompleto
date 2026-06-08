package portaria.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import portaria.project.data.api.ApiConfig
import portaria.project.data.api.provideHttpClient
import portaria.project.data.models.ErrorResponse
import portaria.project.data.models.User

class UserRepository {

    private val client: HttpClient = provideHttpClient()
    private val baseUrl = "${ApiConfig.BASE_URL}/users"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun create(user: User): Result<User> {
        return try {
            println("FRONTEND_API -> Payload do User prestes a ser enviado: Role = '${user.role}'")
            val response: User = client.post(baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(user)
            }.body()
            Result.success(response)
        } catch (e: ResponseException) {
            val body = runCatching { e.response.bodyAsText() }.getOrDefault("")
            val msg = runCatching { json.decodeFromString<ErrorResponse>(body).message }.getOrNull()
            val exception = Exception(msg ?: "Falha ao registrar a conta. Verifique os dados.")
            println("FRONTEND_API -> Falha na criação (HTTP ${e.response.status.value}): ${exception.message}")
            Result.failure(exception)
        } catch (e: Exception) {
            println("FRONTEND_API -> Falha na criação: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAll(): Result<List<User>> {
        return try {
            val response: List<User> = client.get(baseUrl).body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getById(id: String): Result<User> {
        return try {
            val response: User = client.get("$baseUrl/$id").body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(id: String, user: User): Result<Unit> {
        return try {
            client.put("$baseUrl/$id") {
                contentType(ContentType.Application.Json)
                setBody(user)
            }
            Result.success(Unit)
        } catch (e: ResponseException) {
            val body = runCatching { e.response.bodyAsText() }.getOrDefault("")
            val msg = runCatching { json.decodeFromString<ErrorResponse>(body).message }.getOrNull()
            Result.failure(Exception(msg ?: "Erro ao atualizar usuário."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(id: String): Result<Unit> {
        return try {
            client.delete("$baseUrl/$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}