package com.example.demo.service

import com.example.demo.dto.LprResult
import com.example.demo.model.RegistroAcesso
import com.example.demo.repository.VehicleRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime

@Service
class LprService(
    private val vehicleRepository: VehicleRepository,
    private val registroAcessoService: RegistroAcessoService,
    private val servoService: ServoService,
) {
    private val lprUrl = System.getenv("LPR_SERVER_URL") ?: "http://localhost:8001"
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val mapper = ObjectMapper()

    fun triggerScan(): LprResult {
        val lprResponse = callLprServer()
            ?: return LprResult(
                plate = null,
                status = "error",
                residentName = null,
                apartment = null,
                message = "Servidor LPR indisponível. Verifique se o serviço está em execução na porta 8001."
            )

        val plate = lprResponse["plate"] as? String
        val lprStatus = lprResponse["status"] as? String ?: "no_plate"

        if (plate.isNullOrBlank() || lprStatus == "no_plate") {
            return LprResult(
                plate = null,
                status = "no_plate",
                residentName = null,
                apartment = null,
                message = "Nenhuma placa detectada pela câmera."
            )
        }

        val now = LocalDateTime.now().toString()
        val vehicle = vehicleRepository.findByPlate(plate)

        return if (vehicle != null && vehicle.isActive) {
            registroAcessoService.create(
                RegistroAcesso(
                    tipoEvento = "ENTRADA",
                    pessoaNome = "Veículo $plate",
                    pessoaTipo = "VEICULO",
                    veiculoPlaca = plate,
                    dataHora = now,
                    status = "AUTORIZADO",
                    observacao = "Acesso automático via LPR"
                )
            )
            servoService.abrirPortao()
            LprResult(
                plate = plate,
                status = "gate_opened",
                residentName = null,
                apartment = vehicle.apartmentId,
                message = "Portão aberto! Placa $plate autorizada."
            )
        } else {
            registroAcessoService.create(
                RegistroAcesso(
                    tipoEvento = "TENTATIVA",
                    pessoaNome = "Veículo $plate",
                    pessoaTipo = "VEICULO",
                    veiculoPlaca = plate,
                    dataHora = now,
                    status = "NEGADO",
                    observacao = "Placa não cadastrada no sistema"
                )
            )
            LprResult(
                plate = plate,
                status = "denied",
                residentName = null,
                apartment = null,
                message = "Acesso negado. Placa $plate não está cadastrada no sistema."
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun callLprServer(): Map<String, Any?>? = runCatching {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$lprUrl/lpr/scan"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{}"))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) return null
        mapper.readValue(response.body(), Map::class.java) as Map<String, Any?>
    }.getOrNull()
}
