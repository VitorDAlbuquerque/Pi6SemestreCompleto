package com.example.demo.service

/*
 * Testes unitários do LprService — caso de uso: Acesso do Veículo pela Portaria
 *
 * Doubles utilizados:
 *   Mock   - repositórios e serviços externos (Firebase, MQTT, HTTP da câmera)
 *   Stub   - retornos pré-definidos via whenever() para simular cenários
 *   Driver - cada @Test monta o cenário e dispara o método testado
 *
 * CT01 - entrada automática (câmera lê placa cadastrada)           [RN01, RN03]
 * CT02 - liberação manual pelo porteiro (veículo não cadastrado)   [RN03]
 * CT03 - acesso negado, placa desconhecida                         [RN01]
 * CT04 - bloqueio de entrada duplicada (veículo ainda dentro)      [RN03]
 * CT05 - câmera offline, porteiro libera pelo cadastro             [RN01, RN03]
 * CT06 - prestador chega exato no início da janela (08:00)         [RN02]
 * CT07 - prestador chega 1 minuto depois do fim da janela (18:01)  [RN02]
 */

import com.example.demo.model.Prestador
import com.example.demo.model.RegistroAcesso
import com.example.demo.model.User
import com.example.demo.model.Vehicle
import com.example.demo.repository.PrestadorRepository
import com.example.demo.repository.RegistroAcessoRepository
import com.example.demo.repository.UserRepository
import com.example.demo.repository.VehicleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.*

class LprServiceTest {

    // Mocks dos colaboradores — nenhum deles acessa Firebase ou MQTT de verdade
    private val vehicleRepo: VehicleRepository = mock()
    private val registroAcessoService: RegistroAcessoService = mock()
    private val registroAcessoRepo: RegistroAcessoRepository = mock()
    private val prestadorRepo: PrestadorRepository = mock()
    private val userRepo: UserRepository = mock()
    private val servoService: ServoService = mock()
    private val lprScanner: LprScannerClient = mock() // mock do cliente HTTP da câmera LPR

    // Instância padrão do serviço com todos os mocks injetados
    private val sut = LprService(
        vehicleRepo, registroAcessoService, registroAcessoRepo,
        prestadorRepo, userRepo, servoService, lprScanner
    )

    // Cria uma instância do serviço com o relógio travado num horário específico.
    // Necessário nos CT06 e CT07 para testar a janela de horário do prestador
    // sem depender do horário real em que o teste roda.
    private fun sutComHorarioFixo(hour: Int, minute: Int): LprService {
        val instante = LocalDate.of(2026, 6, 11)
            .atTime(hour, minute)
            .toInstant(ZoneOffset.UTC)
        val relogioFixo = Clock.fixed(instante, ZoneOffset.UTC)
        return LprService(
            vehicleRepo, registroAcessoService, registroAcessoRepo,
            prestadorRepo, userRepo, servoService, lprScanner,
            relogioFixo
        )
    }

    // CT01 — câmera lê a placa, veículo está cadastrado e não tem entrada ativa → abre
    @Test
    fun `should_allow_automatic_entry_when_plate_is_registered_ct01`() {
        val placa = "ABC1D23"
        val veiculo = Vehicle(id = "v-001", plate = placa, apartmentId = "apt-101", isActive = true)

        whenever(lprScanner.scan()).thenReturn(mapOf("plate" to placa, "status" to "detected"))
        whenever(vehicleRepo.findByPlate(placa)).thenReturn(veiculo)
        whenever(registroAcessoRepo.findLastByPlate(placa)).thenReturn(null) // sem entrada ativa
        whenever(registroAcessoService.create(any())).thenReturn("log-ct01")

        val resultado = sut.triggerScan()

        assertEquals("gate_opened", resultado.status, "CT01: cancela deve abrir")
        assertEquals(placa, resultado.plate)
        assertEquals("apt-101", resultado.apartment)
        verify(servoService).abrirPortao()

        // confere se gravou o log correto
        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        assertEquals("ENTRADA", captor.firstValue.tipoEvento)
        assertEquals("AUTORIZADO", captor.firstValue.status)
        assertEquals(placa, captor.firstValue.veiculoPlaca)
    }

    // CT02 — veículo não está no sistema, mas o porteiro identifica o morador pelo cadastro e libera
    @Test
    fun `should_allow_manual_entry_by_doorman_when_vehicle_not_registered_but_user_exists_ct02`() {
        val userId = "user-001"
        val placaNaoCadastrada = "XYZ9B99"
        val usuario = User(id = userId, name = "João Silva", role = "MORADOR", apartmentId = "apt-201")

        whenever(userRepo.findById(userId)).thenReturn(usuario)
        whenever(registroAcessoRepo.findLastByPlate(placaNaoCadastrada)).thenReturn(null)
        whenever(registroAcessoService.create(any())).thenReturn("log-ct02")

        val resultado = sut.liberarEntradaManualPorUsuario(userId, placaNaoCadastrada)

        assertEquals("gate_opened", resultado.status, "CT02: porteiro libera manualmente → deve abrir")
        assertEquals(usuario.name, resultado.residentName)
        verify(servoService).abrirPortao()

        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        assertEquals("ENTRADA", captor.firstValue.tipoEvento)
        assertEquals("AUTORIZADO", captor.firstValue.status)
        assertEquals(usuario.name, captor.firstValue.pessoaNome)
    }

    // CT03 — placa não cadastrada, câmera detectou mas o sistema não reconhece → nega
    @Test
    fun `should_deny_access_and_log_when_plate_is_not_registered_ct03`() {
        val placaInvalida = "ZZZ0Z00"

        whenever(lprScanner.scan()).thenReturn(mapOf("plate" to placaInvalida, "status" to "detected"))
        whenever(vehicleRepo.findByPlate(placaInvalida)).thenReturn(null) // não existe no banco
        whenever(registroAcessoService.create(any())).thenReturn("log-ct03")

        val resultado = sut.triggerScan()

        assertEquals("denied", resultado.status, "CT03: placa desconhecida → acesso negado")
        assertEquals(placaInvalida, resultado.plate)
        assertNull(resultado.apartment)
        verify(servoService, never()).abrirPortao()

        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        assertEquals("TENTATIVA", captor.firstValue.tipoEvento)
        assertEquals("NEGADO", captor.firstValue.status)
    }

    // CT04 — veículo já passou pela entrada e ainda não registrou saída → bloqueia segunda entrada
    @Test
    fun `should_deny_duplicate_entry_when_vehicle_already_has_active_entrada_ct04`() {
        val placa = "DEF4G56"
        val veiculo = Vehicle(id = "v-002", plate = placa, apartmentId = "apt-301", isActive = true)
        // stub do histórico: última passagem foi uma entrada que ainda não tem saída
        val ultimaEntradaAtiva = RegistroAcesso(
            id = "log-anterior",
            tipoEvento = "ENTRADA",
            veiculoPlaca = placa,
            status = "AUTORIZADO",
            pessoaNome = "Veículo $placa"
        )

        whenever(lprScanner.scan()).thenReturn(mapOf("plate" to placa, "status" to "detected"))
        whenever(vehicleRepo.findByPlate(placa)).thenReturn(veiculo)
        whenever(registroAcessoRepo.findLastByPlate(placa)).thenReturn(ultimaEntradaAtiva)
        whenever(registroAcessoService.create(any())).thenReturn("log-ct04")

        val resultado = sut.triggerScan()

        assertEquals("duplicate_entry", resultado.status, "CT04: veículo já está dentro → não pode entrar de novo")
        verify(servoService, never()).abrirPortao()

        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        assertEquals("TENTATIVA", captor.firstValue.tipoEvento)
        assertEquals("NEGADO", captor.firstValue.status)
        assert(captor.firstValue.observacao.contains("duplicada")) {
            "CT04: log deve mencionar 'duplicada', veio: '${captor.firstValue.observacao}'"
        }
    }

    // CT05 — câmera offline: scan retorna null, porteiro libera manualmente pela placa
    @Test
    fun `should_report_lpr_offline_then_allow_manual_release_by_doorman_ct05`() {
        val placa = "GHI7H89"
        val veiculo = Vehicle(id = "v-003", plate = placa, apartmentId = "apt-401", isActive = true)

        // parte 1: scan detecta câmera offline
        whenever(lprScanner.scan()).thenReturn(null)
        val resultadoOffline = sut.triggerScan()

        assertEquals("lpr_offline", resultadoOffline.status, "CT05: deve sinalizar que a câmera está fora")
        verify(servoService, never()).abrirPortao()

        // parte 2: porteiro digita a placa manualmente e libera
        whenever(vehicleRepo.findByPlate(placa)).thenReturn(veiculo)
        whenever(registroAcessoRepo.findLastByPlate(placa)).thenReturn(null)
        whenever(registroAcessoService.create(any())).thenReturn("log-ct05")

        val resultadoManual = sut.liberarEntradaManual(placa)

        assertEquals("gate_opened", resultadoManual.status, "CT05: liberação manual deve abrir")
        verify(servoService).abrirPortao()

        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        assertEquals("ENTRADA", captor.firstValue.tipoEvento)
        assertEquals("AUTORIZADO", captor.firstValue.status)
        assert(captor.firstValue.observacao.contains("manual")) {
            "CT05: log deve indicar acesso manual, veio: '${captor.firstValue.observacao}'"
        }
    }

    // CT06 — prestador chega exatamente no início da janela (08:00) → deve entrar
    // O limite inferior é inclusivo: 08:00 == 08:00 é permitido
    @Test
    fun `should_allow_service_provider_entry_exactly_at_start_of_allowed_window_ct06`() {
        val placa = "PRE1A11"
        val prestadorId = "prest-001"
        val prestador = Prestador(
            id = prestadorId,
            companyName = "Manutenção Predial LTDA",
            employeeName = "Carlos Souza",
            allowedStartTime = "08:00",
            allowedEndTime = "18:00"
        )
        val veiculo = Vehicle(id = "v-004", plate = placa, prestadorId = prestadorId, isActive = true)

        val sutComHorario = sutComHorarioFixo(hour = 8, minute = 0) // relógio travado em 08:00

        whenever(lprScanner.scan()).thenReturn(mapOf("plate" to placa, "status" to "detected"))
        whenever(vehicleRepo.findByPlate(placa)).thenReturn(veiculo)
        whenever(prestadorRepo.findById(prestadorId)).thenReturn(prestador)
        whenever(registroAcessoRepo.findLastByPlate(placa)).thenReturn(null)
        whenever(registroAcessoService.create(any())).thenReturn("log-ct06")

        val resultado = sutComHorario.triggerScan()

        assertEquals("gate_opened", resultado.status, "CT06: 08:00 é o início da janela, deve ser permitido")
        verify(servoService).abrirPortao()

        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        assertEquals("AUTORIZADO", captor.firstValue.status)
    }

    // CT07 — prestador chega 1 minuto depois do fim da janela (18:01) → nega
    // O limite superior é exclusivo: 18:01 > 18:00 é bloqueado
    @Test
    fun `should_deny_service_provider_entry_one_minute_after_end_of_allowed_window_ct07`() {
        val placa = "PRE2B22"
        val prestadorId = "prest-002"
        val prestador = Prestador(
            id = prestadorId,
            companyName = "Limpeza & Conservação ME",
            employeeName = "Ana Lima",
            allowedStartTime = "08:00",
            allowedEndTime = "18:00"
        )
        val veiculo = Vehicle(id = "v-005", plate = placa, prestadorId = prestadorId, isActive = true)

        val sutComHorario = sutComHorarioFixo(hour = 18, minute = 1) // relógio travado em 18:01

        whenever(lprScanner.scan()).thenReturn(mapOf("plate" to placa, "status" to "detected"))
        whenever(vehicleRepo.findByPlate(placa)).thenReturn(veiculo)
        whenever(prestadorRepo.findById(prestadorId)).thenReturn(prestador)
        whenever(registroAcessoService.create(any())).thenReturn("log-ct07")

        val resultado = sutComHorario.triggerScan()

        assertEquals("denied_by_time", resultado.status, "CT07: 18:01 está fora da janela, deve ser negado")
        assertEquals(placa, resultado.plate)
        verify(servoService, never()).abrirPortao()

        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        assertEquals("TENTATIVA", captor.firstValue.tipoEvento)
        assertEquals("NEGADO", captor.firstValue.status)
        assert(captor.firstValue.observacao.contains("horário")) {
            "CT07: log deve mencionar 'horário', veio: '${captor.firstValue.observacao}'"
        }
    }
}
