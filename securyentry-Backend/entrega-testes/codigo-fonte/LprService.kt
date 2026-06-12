package com.example.demo.service

import com.example.demo.dto.LprResult
import com.example.demo.model.RegistroAcesso
import com.example.demo.repository.PrestadorRepository
import com.example.demo.repository.RegistroAcessoRepository
import com.example.demo.repository.UserRepository
import com.example.demo.repository.VehicleRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime

// Serviço principal da portaria — decide se o portão abre ou não.
// Três regras guiam tudo:
//   RN01: placa tem que estar cadastrada e ativa
//   RN02: prestadores só entram dentro do horário combinado
//   RN03: não pode entrar se já tem uma entrada sem saída registrada
@Service
class LprService(
    private val vehicleRepository: VehicleRepository,
    private val registroAcessoService: RegistroAcessoService,
    private val registroAcessoRepository: RegistroAcessoRepository,
    private val prestadorRepository: PrestadorRepository,
    private val userRepository: UserRepository,
    private val servoService: ServoService,
    private val lprScannerClient: LprScannerClient,
    private val clock: Clock = Clock.systemDefaultZone()
) {

    // leitura automática via câmera
    fun triggerScan(): LprResult {
        val lprResponse = lprScannerClient.scan()
            ?: return LprResult(
                plate = null,
                status = "lpr_offline",
                residentName = null,
                apartment = null,
                message = "Módulo LPR indisponível. Porteiro pode liberar manualmente."
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

        return evaluateAccess(plate, manual = false)
    }

    // porteiro libera manualmente quando a câmera tá offline (CT05)
    fun liberarEntradaManual(plate: String): LprResult =
        evaluateAccess(plate, manual = true)

    // porteiro localiza o morador pelo cadastro quando o veículo não está registrado (CT02)
    fun liberarEntradaManualPorUsuario(userId: String, plate: String?): LprResult {
        val now = LocalDateTime.now(clock).toString()

        val user = userRepository.findById(userId)
            ?: return LprResult(
                plate = plate,
                status = "denied",
                residentName = null,
                apartment = null,
                message = "Usuário não encontrado. Acesso negado."
            )

        // RN03 — verifica status (ENTROU/SAIU) mesmo na liberação manual
        val lastAccess = plate?.let { registroAcessoRepository.findLastByPlate(it) }
        if (lastAccess != null && lastAccess.tipoEvento == "ENTRADA" && lastAccess.status == "AUTORIZADO") {
            registroAcessoService.create(
                RegistroAcesso(
                    tipoEvento = "TENTATIVA",
                    pessoaNome = user.name,
                    pessoaTipo = user.role,
                    veiculoPlaca = plate ?: "",
                    dataHora = now,
                    status = "NEGADO",
                    observacao = "Tentativa de entrada duplicada via liberação manual"
                )
            )
            return LprResult(
                plate = plate,
                status = "duplicate_entry",
                residentName = user.name,
                apartment = null,
                message = "Acesso negado. Usuário ${user.name} já possui entrada ativa."
            )
        }

        registroAcessoService.create(
            RegistroAcesso(
                tipoEvento = "ENTRADA",
                pessoaNome = user.name,
                pessoaTipo = user.role,
                veiculoPlaca = plate ?: "",
                dataHora = now,
                status = "AUTORIZADO",
                observacao = "Acesso manual via porteiro - veículo não cadastrado"
            )
        )
        servoService.abrirPortao()
        return LprResult(
            plate = plate,
            status = "gate_opened",
            residentName = user.name,
            apartment = user.apartmentId,
            message = "Portão aberto! Acesso manual autorizado para ${user.name}."
        )
    }

    // aplica RN01 → RN02 → RN03 em sequência e decide se abre ou nega
    private fun evaluateAccess(plate: String, manual: Boolean): LprResult {
        val now = LocalDateTime.now(clock).toString()

        // RN01 — placa deve estar cadastrada e ativa
        val vehicle = vehicleRepository.findByPlate(plate)
        if (vehicle == null || !vehicle.isActive) {
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
            return LprResult(
                plate = plate,
                status = "denied",
                residentName = null,
                apartment = null,
                message = "Acesso negado. Placa $plate não está cadastrada no sistema."
            )
        }

        // RN02 — para prestadores, valida janela de horário (CT06 / CT07)
        if (vehicle.prestadorId.isNotBlank()) {
            val prestador = prestadorRepository.findById(vehicle.prestadorId)
            if (prestador != null) {
                val currentTime = LocalTime.now(clock)
                val startTime = LocalTime.parse(prestador.allowedStartTime)
                val endTime = LocalTime.parse(prestador.allowedEndTime)
                // 08:00 == 08:00 entra (inclusivo), 18:01 > 18:00 nega (exclusivo)
                if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
                    registroAcessoService.create(
                        RegistroAcesso(
                            tipoEvento = "TENTATIVA",
                            pessoaNome = prestador.employeeName,
                            pessoaTipo = "PRESTADOR",
                            veiculoPlaca = plate,
                            dataHora = now,
                            status = "NEGADO",
                            observacao = "Acesso negado por horário: fora da janela ${prestador.allowedStartTime}-${prestador.allowedEndTime}"
                        )
                    )
                    return LprResult(
                        plate = plate,
                        status = "denied_by_time",
                        residentName = null,
                        apartment = null,
                        message = "Acesso negado. Horário fora da janela permitida (${prestador.allowedStartTime}-${prestador.allowedEndTime})."
                    )
                }
            }
        }

        // RN03 — status deve ser SAIU; bloqueia entrada duplicada (CT04)
        val lastAccess = registroAcessoRepository.findLastByPlate(plate)
        if (lastAccess != null && lastAccess.tipoEvento == "ENTRADA" && lastAccess.status == "AUTORIZADO") {
            registroAcessoService.create(
                RegistroAcesso(
                    tipoEvento = "TENTATIVA",
                    pessoaNome = "Veículo $plate",
                    pessoaTipo = "VEICULO",
                    veiculoPlaca = plate,
                    dataHora = now,
                    status = "NEGADO",
                    observacao = "Tentativa de entrada duplicada: veículo já registrado como ENTROU"
                )
            )
            return LprResult(
                plate = plate,
                status = "duplicate_entry",
                residentName = null,
                apartment = null,
                message = "Acesso negado. Veículo $plate já possui entrada ativa sem saída registrada."
            )
        }

        // Todas as regras passaram → abre portão e registra AUTORIZADO
        val observacao = if (manual) "Acesso manual via porteiro" else "Acesso automático via LPR"
        registroAcessoService.create(
            RegistroAcesso(
                tipoEvento = "ENTRADA",
                pessoaNome = "Veículo $plate",
                pessoaTipo = "VEICULO",
                veiculoPlaca = plate,
                dataHora = now,
                status = "AUTORIZADO",
                observacao = observacao
            )
        )
        servoService.abrirPortao()
        return LprResult(
            plate = plate,
            status = "gate_opened",
            residentName = null,
            apartment = vehicle.apartmentId,
            message = "Portão aberto! Placa $plate autorizada."
        )
    }
}
