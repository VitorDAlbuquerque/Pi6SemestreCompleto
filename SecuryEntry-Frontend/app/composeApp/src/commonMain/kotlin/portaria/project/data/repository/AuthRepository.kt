package portaria.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import portaria.project.data.api.ApiConfig
import portaria.project.data.models.LoginRequest
import portaria.project.data.models.LoginResponse

class AuthRepository {

    private val client = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
                encodeDefaults = true
                explicitNulls = false
            })
        }
    }

    private val baseUrl = "${ApiConfig.BASE_URL}/auth"

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response: LoginResponse = client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            Result.success(response)
        } catch (e: ClientRequestException) {
            val errorMessage = when (e.response.status.value) {
                404 -> "USUARIO_NAO_ENCONTRADO"
                400 -> "SENHA_INCORRETA"
                403 -> "CONTA_DESATIVADA"
                409 -> "CONTA_DESATIVADA"
                else -> "ERRO_DESCONHECIDO"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            println("Erro de conexão no login: ${e.message}")
            Result.failure(Exception("ERRO_DE_CONEXAO"))
        }
    }
}