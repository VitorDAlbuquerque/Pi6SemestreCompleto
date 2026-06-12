package com.example.demo.service

/**
 * =============================================================================
 * TESTES UNITÁRIOS — LprService (Controle de Acesso / Portaria)
 * =============================================================================
 *
 * Pacote  : com.example.demo.service
 * Classe  : LprServiceTest
 * Framework: JUnit 5 (Jupiter) + Mockito-Kotlin 5.4
 *
 * Recursos de teste adotados (conforme terminologia de Pressman/Sommerville):
 *
 *  • MOCK    — objeto que registra chamadas e permite verificar interações.
 *              Usado para: VehicleRepository, RegistroAcessoRepository,
 *              PrestadorRepository, UserRepository, RegistroAcessoService,
 *              ServoService e LprScannerClient.
 *
 *  • STUB    — configuração de retorno pré-programado em um mock via `whenever`.
 *              Usado para simular respostas do banco de dados (Firebase), do
 *              hardware de câmera (LPR) e do módulo MQTT (cancela).
 *
 *  • DRIVER  — código que orquestra o cenário e aciona a unidade sob teste.
 *              Cada método @Test atua como driver do caso de teste respectivo.
 *
 *  • OBJETO FAKE DE TEMPO — `java.time.Clock.fixed(...)` usado nos CT06/CT07
 *              para controlar o horário sem depender do relógio do sistema.
 *
 * Organização por Caso de Teste:
 *   CT01 — Entrada automática permitida
 *   CT02 — Liberação manual (veículo não cadastrado, usuário cadastrado)
 *   CT03 — Acesso negado (placa não cadastrada / horário inválido)
 *   CT04 — Entrada duplicada bloqueada (status já ENTROU)
 *   CT05 — LPR offline → liberação manual pelo porteiro
 *   CT06 — Prestador no limite inferior do horário (08:00 → permitido)
 *   CT07 — Prestador após o limite superior do horário (18:01 → negado)
 * =============================================================================
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

    // =========================================================================
    // MOCKS — substitutos dos colaboradores externos (BD, hardware, serviços)
    // Nenhum destes acessa o Firebase ou o broker MQTT real durante os testes.
    // =========================================================================

    /** MOCK: repositório de veículos (substitui chamadas ao Firestore) */
    private val vehicleRepo: VehicleRepository = mock()

    /** MOCK: serviço de auditoria que persiste o log de cada evento de acesso */
    private val registroAcessoService: RegistroAcessoService = mock()

    /** MOCK: repositório de acesso (consulta ao histórico para checar ENTROU/SAIU) */
    private val registroAcessoRepo: RegistroAcessoRepository = mock()

    /** MOCK: repositório de prestadores (para leitura da janela de horário) */
    private val prestadorRepo: PrestadorRepository = mock()

    /** MOCK: repositório de usuários (utilizado na liberação manual por usuário) */
    private val userRepo: UserRepository = mock()

    /** MOCK: serviço de servo/cancela — substitui a publicação MQTT real */
    private val servoService: ServoService = mock()

    /** MOCK: cliente HTTP do módulo LPR (câmera) — evita chamadas de rede reais */
    private val lprScanner: LprScannerClient = mock()

    // =========================================================================
    // INSTÂNCIA DA UNIDADE SOB TESTE (SUT)
    // Construída diretamente (sem container Spring) com todos os mocks injetados.
    // =========================================================================

    /** SUT padrão: usa relógio do sistema (adequado para CT01–CT05) */
    private val sut = LprService(
        vehicleRepo, registroAcessoService, registroAcessoRepo,
        prestadorRepo, userRepo, servoService, lprScanner
    )

    /**
     * DRIVER AUXILIAR — cria uma instância do SUT com horário fixo.
     * Utilizado exclusivamente nos CT06 e CT07 para controlar a hora do dia
     * sem depender do relógio real do ambiente de CI/CD.
     *
     * @param hour   hora a ser simulada (0–23, UTC)
     * @param minute minuto a ser simulado (0–59)
     */
    private fun sutComHorarioFixo(hour: Int, minute: Int): LprService {
        val instanteFixo = LocalDate.of(2026, 6, 11)
            .atTime(hour, minute)
            .toInstant(ZoneOffset.UTC)
        val relogioFixo = Clock.fixed(instanteFixo, ZoneOffset.UTC) // FAKE DE TEMPO
        return LprService(
            vehicleRepo, registroAcessoService, registroAcessoRepo,
            prestadorRepo, userRepo, servoService, lprScanner,
            relogioFixo
        )
    }

    // =========================================================================
    // CT01 — Fluxo principal: entrada automática permitida
    // RN01: placa cadastrada e ativa
    // RN03: nenhum registro anterior (status = SAIU implícito)
    // Saída esperada: status = "gate_opened", portão abre, log AUTORIZADO gravado.
    // =========================================================================

    @Test
    fun `should_allow_automatic_entry_when_plate_is_registered_ct01`() {
        // --- Dados de Teste (Entidades) ---
        val placa = "ABC1D23"
        val veiculo = Vehicle(id = "v-001", plate = placa, apartmentId = "apt-101", isActive = true)

        // --- STUB: câmera reconhece a placa com sucesso ---
        whenever(lprScanner.scan()).thenReturn(mapOf("plate" to placa, "status" to "detected"))

        // --- STUB: veículo encontrado no banco (RN01 satisfeita) ---
        whenever(vehicleRepo.findByPlate(placa)).thenReturn(veiculo)

        // --- STUB: nenhum registro anterior → status SAIU (RN03 satisfeita) ---
        whenever(registroAcessoRepo.findLastByPlate(placa)).thenReturn(null)

        // --- STUB: salva log de acesso ---
        whenever(registroAcessoService.create(any())).thenReturn("log-ct01")

        // --- DRIVER: aciona a unidade sob teste ---
        val resultado = sut.triggerScan()

        // --- Assertivas ---
        assertEquals("gate_opened", resultado.status, "CT01: cancela deve abrir")
        assertEquals(placa, resultado.plate, "CT01: placa retornada deve ser a mesma")
        assertEquals("apt-101", resultado.apartment, "CT01: apartamento do veículo deve constar no resultado")

        // Verifica que o portão foi efetivamente aberto (comando enviado ao hardware)
        verify(servoService).abrirPortao()

        // Verifica que o log de ENTRADA AUTORIZADO foi gravado
        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        val logGravado = captor.firstValue
        assertEquals("ENTRADA", logGravado.tipoEvento, "CT01: tipo do evento deve ser ENTRADA")
        assertEquals("AUTORIZADO", logGravado.status, "CT01: status do log deve ser AUTORIZADO")
        assertEquals(placa, logGravado.veiculoPlaca, "CT01: log deve referenciar a placa correta")
    }

    // =========================================================================
    // CT02 — Fluxo alternativo: liberação manual (veículo não cadastrado, usuário sim)
    // RN01: placa NÃO cadastrada → porteiro busca por nome do usuário
    // RN03: status do usuário é SAIU (sem entrada ativa para a placa)
    // Saída esperada: porteiro libera, status = "gate_opened", log AUTORIZADO.
    // =========================================================================

    @Test
    fun `should_allow_manual_entry_by_doorman_when_vehicle_not_registered_but_user_exists_ct02`() {
        // --- Dados de Teste ---
        val userId = "user-001"
        val placaNaoCadastrada = "XYZ9B99"
        val usuario = User(id = userId, name = "João Silva", role = "MORADOR", apartmentId = "apt-201")

        // --- STUB: porteiro encontrou o usuário pelo cadastro manual ---
        whenever(userRepo.findById(userId)).thenReturn(usuario)

        // --- STUB: nenhuma entrada anterior para a placa informada (status SAIU) ---
        whenever(registroAcessoRepo.findLastByPlate(placaNaoCadastrada)).thenReturn(null)

        // --- STUB: salva log de acesso ---
        whenever(registroAcessoService.create(any())).thenReturn("log-ct02")

        // --- DRIVER: porteiro confirma usuário e aciona liberação manual ---
        val resultado = sut.liberarEntradaManualPorUsuario(userId, placaNaoCadastrada)

        // --- Assertivas ---
        assertEquals("gate_opened", resultado.status, "CT02: cancela deve abrir via liberação manual")
        assertEquals(usuario.name, resultado.residentName, "CT02: nome do usuário deve constar no resultado")

        // Verifica que o portão foi aberto
        verify(servoService).abrirPortao()

        // Verifica que o log registra ENTRADA AUTORIZADO com o nome do usuário
        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        val logGravado = captor.firstValue
        assertEquals("ENTRADA", logGravado.tipoEvento, "CT02: evento deve ser ENTRADA")
        assertEquals("AUTORIZADO", logGravado.status, "CT02: status deve ser AUTORIZADO")
        assertEquals(usuario.name, logGravado.pessoaNome, "CT02: log deve ter o nome do usuário")
    }

    // =========================================================================
    // CT03 — Fluxo de exceção: veículo/usuário não cadastrado
    // RN01: placa não encontrada no sistema
    // Saída esperada: status = "denied", portão permanece fechado,
    //                 log de "tentativa negada" gravado.
    // =========================================================================

    @Test
    fun `should_deny_access_and_log_when_plate_is_not_registered_ct03`() {
        // --- Dados de Teste ---
        val placaInvalida = "ZZZ0Z00"

        // --- STUB: câmera detectou uma placa ---
        whenever(lprScanner.scan()).thenReturn(mapOf("plate" to placaInvalida, "status" to "detected"))

        // --- STUB: placa NÃO está cadastrada no banco (RN01 violada) ---
        whenever(vehicleRepo.findByPlate(placaInvalida)).thenReturn(null)

        // --- STUB: salva log de acesso ---
        whenever(registroAcessoService.create(any())).thenReturn("log-ct03")

        // --- DRIVER ---
        val resultado = sut.triggerScan()

        // --- Assertivas ---
        assertEquals("denied", resultado.status, "CT03: acesso deve ser negado")
        assertEquals(placaInvalida, resultado.plate, "CT03: placa inválida deve constar no resultado")
        assertNull(resultado.apartment, "CT03: apartamento deve ser nulo para placa desconhecida")

        // Verifica que o portão NÃO foi aberto
        verify(servoService, never()).abrirPortao()

        // Verifica que o log de TENTATIVA NEGADA foi gravado
        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        val logGravado = captor.firstValue
        assertEquals("TENTATIVA", logGravado.tipoEvento, "CT03: deve registrar tentativa de acesso")
        assertEquals("NEGADO", logGravado.status, "CT03: status do log deve ser NEGADO")
    }

    // =========================================================================
    // CT04 — Fluxo de exceção: entrada duplicada (status ENTROU)
    // RN01: placa cadastrada e ativa
    // RN03: último registro é ENTRADA AUTORIZADO → usuário ainda está "ENTROU"
    // Saída esperada: status = "duplicate_entry", portão fechado,
    //                 log de "entrada duplicada" gravado.
    // =========================================================================

    @Test
    fun `should_deny_duplicate_entry_when_vehicle_already_has_active_entrada_ct04`() {
        // --- Dados de Teste ---
        val placa = "DEF4G56"
        val veiculo = Vehicle(id = "v-002", plate = placa, apartmentId = "apt-301", isActive = true)
        // Simula o estado ENTROU: última passagem foi uma ENTRADA AUTORIZADA que ainda não saiu
        val ultimaEntradaAtiva = RegistroAcesso(
            id = "log-anterior",
            tipoEvento = "ENTRADA",
            veiculoPlaca = placa,
            status = "AUTORIZADO",
            pessoaNome = "Veículo $placa"
        )

        // --- STUB: câmera reconhece a placa ---
        whenever(lprScanner.scan()).thenReturn(mapOf("plate" to placa, "status" to "detected"))

        // --- STUB: veículo cadastrado e ativo (RN01 satisfeita) ---
        whenever(vehicleRepo.findByPlate(placa)).thenReturn(veiculo)

        // --- STUB: último registro indica que o veículo JÁ ESTÁ DENTRO (RN03 violada) ---
        whenever(registroAcessoRepo.findLastByPlate(placa)).thenReturn(ultimaEntradaAtiva)

        // --- STUB: salva log de acesso ---
        whenever(registroAcessoService.create(any())).thenReturn("log-ct04")

        // --- DRIVER ---
        val resultado = sut.triggerScan()

        // --- Assertivas ---
        assertEquals("duplicate_entry", resultado.status, "CT04: deve detectar entrada duplicada")

        // Verifica que o portão NÃO foi aberto
        verify(servoService, never()).abrirPortao()

        // Verifica que o log de TENTATIVA com observação de entrada duplicada foi gravado
        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        val logGravado = captor.firstValue
        assertEquals("TENTATIVA", logGravado.tipoEvento, "CT04: deve registrar tentativa")
        assertEquals("NEGADO", logGravado.status, "CT04: status deve ser NEGADO")
        assert(logGravado.observacao.contains("duplicada")) {
            "CT04: observação do log deve mencionar 'duplicada', mas foi: '${logGravado.observacao}'"
        }
    }

    // =========================================================================
    // CT05 — Fluxo alternativo: módulo LPR fora do ar → liberação manual
    // Parte 1: triggerScan() detecta que o hardware está indisponível.
    // Parte 2: porteiro localiza veículo e aciona liberarEntradaManual(plate).
    // RN03: status do veículo é SAIU.
    // Saída esperada (parte 2): status = "gate_opened", log com observação "manual".
    // =========================================================================

    @Test
    fun `should_report_lpr_offline_then_allow_manual_release_by_doorman_ct05`() {
        val placa = "GHI7H89"
        val veiculo = Vehicle(id = "v-003", plate = placa, apartmentId = "apt-401", isActive = true)

        // --- Parte 1: STUB que simula falha total do módulo LPR (retorna null) ---
        whenever(lprScanner.scan()).thenReturn(null)

        // DRIVER: aciona o scan
        val resultadoOffline = sut.triggerScan()

        // Assertiva da parte 1: sistema informa indisponibilidade, portão permanece fechado
        assertEquals("lpr_offline", resultadoOffline.status, "CT05: deve detectar LPR offline")
        verify(servoService, never()).abrirPortao()

        // --- Parte 2: porteiro localiza veículo e libera manualmente ---

        // STUB: veículo cadastrado (porteiro buscou manualmente)
        whenever(vehicleRepo.findByPlate(placa)).thenReturn(veiculo)

        // STUB: status SAIU — sem entrada ativa (RN03 satisfeita)
        whenever(registroAcessoRepo.findLastByPlate(placa)).thenReturn(null)

        // STUB: salva log
        whenever(registroAcessoService.create(any())).thenReturn("log-ct05")

        // DRIVER: porteiro aciona liberação manual pela placa
        val resultadoManual = sut.liberarEntradaManual(placa)

        // Assertivas da parte 2
        assertEquals("gate_opened", resultadoManual.status, "CT05: liberação manual deve abrir o portão")
        verify(servoService).abrirPortao()

        // Verifica que o log registra acesso manual com observação "manual"
        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        val logGravado = captor.firstValue
        assertEquals("ENTRADA", logGravado.tipoEvento, "CT05: evento deve ser ENTRADA")
        assertEquals("AUTORIZADO", logGravado.status, "CT05: status deve ser AUTORIZADO")
        assert(logGravado.observacao.contains("manual")) {
            "CT05: observação deve indicar acesso manual, mas foi: '${logGravado.observacao}'"
        }
    }

    // =========================================================================
    // CT06 — Limite de horário do prestador: chegada exatamente no início (08:00)
    // RN01: placa de prestador cadastrada e ativa
    // RN02: horário atual == limite inferior da janela (08:00) → INCLUSIVO → PERMITIDO
    // RN03: status SAIU (sem entrada ativa)
    // Saída esperada: status = "gate_opened", cancela liberada.
    // =========================================================================

    @Test
    fun `should_allow_service_provider_entry_exactly_at_start_of_allowed_window_ct06`() {
        // --- Dados de Teste ---
        val placa = "PRE1A11"
        val prestadorId = "prest-001"
        val prestador = Prestador(
            id = prestadorId,
            companyName = "Manutenção Predial LTDA",
            employeeName = "Carlos Souza",
            allowedStartTime = "08:00",   // janela: 08:00–18:00
            allowedEndTime = "18:00"
        )
        val veiculo = Vehicle(
            id = "v-004", plate = placa,
            prestadorId = prestadorId,    // vínculo com prestador
            isActive = true
        )

        // --- FAKE DE TEMPO: relógio fixo em 08:00 (exato limite inferior) ---
        val sutComHorario = sutComHorarioFixo(hour = 8, minute = 0)

        // --- STUBs ---
        whenever(lprScanner.scan()).thenReturn(mapOf("plate" to placa, "status" to "detected"))
        whenever(vehicleRepo.findByPlate(placa)).thenReturn(veiculo)
        whenever(prestadorRepo.findById(prestadorId)).thenReturn(prestador)
        whenever(registroAcessoRepo.findLastByPlate(placa)).thenReturn(null) // SAIU
        whenever(registroAcessoService.create(any())).thenReturn("log-ct06")

        // --- DRIVER ---
        val resultado = sutComHorario.triggerScan()

        // --- Assertivas ---
        assertEquals(
            "gate_opened", resultado.status,
            "CT06: horário 08:00 é o limite inferior inclusivo — deve ser PERMITIDO"
        )
        verify(servoService).abrirPortao()

        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        assertEquals("AUTORIZADO", captor.firstValue.status, "CT06: log deve ser AUTORIZADO")
    }

    // =========================================================================
    // CT07 — Limite de horário do prestador: chegada após o fim (18:01)
    // RN01: placa de prestador cadastrada e ativa
    // RN02: horário atual > limite superior da janela (18:01 > 18:00) → NEGADO
    // Saída esperada: status = "denied_by_time", cancela permanece fechada,
    //                 log de "acesso negado por horário" gravado.
    // =========================================================================

    @Test
    fun `should_deny_service_provider_entry_one_minute_after_end_of_allowed_window_ct07`() {
        // --- Dados de Teste ---
        val placa = "PRE2B22"
        val prestadorId = "prest-002"
        val prestador = Prestador(
            id = prestadorId,
            companyName = "Limpeza & Conservação ME",
            employeeName = "Ana Lima",
            allowedStartTime = "08:00",
            allowedEndTime = "18:00"
        )
        val veiculo = Vehicle(
            id = "v-005", plate = placa,
            prestadorId = prestadorId,
            isActive = true
        )

        // --- FAKE DE TEMPO: relógio fixo em 18:01 (um minuto após o fim da janela) ---
        val sutComHorario = sutComHorarioFixo(hour = 18, minute = 1)

        // --- STUBs ---
        whenever(lprScanner.scan()).thenReturn(mapOf("plate" to placa, "status" to "detected"))
        whenever(vehicleRepo.findByPlate(placa)).thenReturn(veiculo)
        whenever(prestadorRepo.findById(prestadorId)).thenReturn(prestador)
        whenever(registroAcessoService.create(any())).thenReturn("log-ct07")

        // --- DRIVER ---
        val resultado = sutComHorario.triggerScan()

        // --- Assertivas ---
        assertEquals(
            "denied_by_time", resultado.status,
            "CT07: horário 18:01 está após o limite superior — deve ser NEGADO"
        )
        assertEquals(placa, resultado.plate, "CT07: placa deve constar no resultado")

        // Portão NÃO deve abrir
        verify(servoService, never()).abrirPortao()

        // Log de TENTATIVA NEGADA por horário deve ser gravado
        val captor = argumentCaptor<RegistroAcesso>()
        verify(registroAcessoService).create(captor.capture())
        val logGravado = captor.firstValue
        assertEquals("TENTATIVA", logGravado.tipoEvento, "CT07: deve registrar tentativa")
        assertEquals("NEGADO", logGravado.status, "CT07: status deve ser NEGADO")
        assert(logGravado.observacao.contains("horário")) {
            "CT07: observação deve mencionar 'horário', mas foi: '${logGravado.observacao}'"
        }
    }
}
