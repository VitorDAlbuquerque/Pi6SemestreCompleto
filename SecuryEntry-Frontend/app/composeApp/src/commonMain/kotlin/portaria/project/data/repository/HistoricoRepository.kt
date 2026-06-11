package portaria.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import portaria.project.data.api.ApiConfig
import portaria.project.data.models.RegistroAcesso

class HistoricoRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val baseUrl = "${ApiConfig.BASE_URL}/historico"

    suspend fun getHistorico(): Result<List<RegistroAcesso>> = try {
        val response: List<RegistroAcesso> = client.get(baseUrl).body()
        Result.success(response.sortedByDescending { it.dataHora })
    } catch (e: Exception) {
        Result.failure(Exception("Não foi possível carregar a auditoria: ${e.message}"))
    }
}