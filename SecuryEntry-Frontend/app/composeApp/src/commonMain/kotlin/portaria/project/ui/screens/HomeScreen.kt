package portaria.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import portaria.project.data.api.RepositorioRemoto
import portaria.project.navigation.AppNavigator
import portaria.project.ui.components.SaaSLayout
import portaria.project.ui.viewmodels.HistoricoViewModel
import portaria.project.data.models.RegistroAcesso

// Tela principal e métricas operacionais (HomeScreen)
@Composable
fun HomeScreen(navigator: AppNavigator) {
    val historicoViewModel = remember { HistoricoViewModel() }
    val historicoState by historicoViewModel.uiState.collectAsState()

    val visitorRepository = remember { portaria.project.data.repository.VisitorRepository() }
    val encomendaRepository = remember { portaria.project.data.repository.EncomendaRepository() }
    val veiculoRepository = remember { portaria.project.data.repository.VehicleRepository() }
    val repositorioRemoto = remember { RepositorioRemoto() }
    val coroutineScope = rememberCoroutineScope()

    var totalVisitors by remember { mutableStateOf(0) }
    var pendingEncomendas by remember { mutableStateOf(0) }
    var totalVehicles by remember { mutableStateOf(0) }
    var portaoLoading by remember { mutableStateOf(false) }
    var portaoMensagem by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        historicoViewModel.loadHistorico()
        visitorRepository.getAll().onSuccess { totalVisitors = it.size }
        encomendaRepository.getAll().onSuccess { pendingEncomendas = it.count { enc -> !enc.deliveredToResident } }
        veiculoRepository.getAll().onSuccess { totalVehicles = it.size }
    }

    SaaSLayout(title = "Dashboard Operacional", navigator = navigator) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Visão Geral Diária",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Métricas em tempo real da portaria e acessos.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "MONITOR ATIVO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            val totalAcessos = historicoState.registros.size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Acessos Registrados",
                    value = totalAcessos.toString(),
                    trend = "Registros em tempo real",
                    isTrendPositive = true,
                    icon = Icons.Default.SwapVert,
                    gradientStart = Color(0xFF6366F1),
                    gradientEnd = Color(0xFF4F46E5)
                )
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Visitantes Ativos",
                    value = totalVisitors.toString(),
                    trend = "Visitantes cadastrados",
                    isTrendPositive = true,
                    icon = Icons.Default.Face,
                    gradientStart = Color(0xFF3B82F6),
                    gradientEnd = Color(0xFF2563EB)
                )
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Encomendas Pendentes",
                    value = pendingEncomendas.toString(),
                    trend = "Aguardando retirada",
                    isTrendPositive = pendingEncomendas > 0,
                    icon = Icons.Default.Email,
                    gradientStart = Color(0xFFF59E0B),
                    gradientEnd = Color(0xFFD97706)
                )
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Alertas LPR",
                    value = "0",
                    trend = "$totalVehicles placas vigiadas",
                    isTrendPositive = true,
                    isCritical = false,
                    icon = Icons.Default.Warning,
                    gradientStart = Color(0xFFEF4444),
                    gradientEnd = Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            portaoLoading = true
                            portaoMensagem = null
                            repositorioRemoto.runCatching { abrirPortao() }
                                .onSuccess { portaoMensagem = "Portão aberto com sucesso!" }
                                .onFailure { portaoMensagem = "Erro: ${it.message}" }
                            portaoLoading = false
                        }
                    },
                    enabled = !portaoLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    if (portaoLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (portaoLoading) "Abrindo..." else "Abrir Portão",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                portaoMensagem?.let { msg ->
                    val isError = msg.startsWith("Erro")
                    Text(
                        text = msg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF10B981),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                                else Color(0xFF10B981).copy(alpha = 0.08f)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Fluxo de Acesso Recente",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Registro cronológico em tempo real dos últimos eventos de acesso.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (historicoState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (historicoState.registros.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum registro de acesso recente no banco de dados.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                val listLogs = historicoState.registros.take(10)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listLogs) { reg ->
                        AcessoRealRow(reg)
                    }
                }
            }
        }
    }
}

// Componente de card de métricas SaaS (DashboardCard)
@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    trend: String,
    isTrendPositive: Boolean,
    isCritical: Boolean = false,
    icon: ImageVector,
    gradientStart: Color,
    gradientEnd: Color
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(gradientStart, gradientEnd))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val trendColor = when {
                    isCritical -> MaterialTheme.colorScheme.error
                    isTrendPositive -> Color(0xFF10B981)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(trendColor)
                )

                Text(
                    text = trend,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = trendColor
                )
            }
        }
    }
}

// Componente de registro de acesso em tempo real (AcessoRealRow)
@Composable
fun AcessoRealRow(reg: RegistroAcesso) {
    val isEntrada = reg.tipoEvento == "ENTRADA"
    val isAutorizado = reg.status == "AUTORIZADO"
    val badgeColor = if (!isAutorizado) Color(0xFFEF4444) else if (isEntrada) Color(0xFF10B981) else Color(0xFF94A3B8)
    val statusBg = if (!isAutorizado) Color(0xFFEF4444).copy(alpha = 0.08f) else if (isEntrada) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFF94A3B8).copy(alpha = 0.08f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = reg.pessoaNome.take(2).uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reg.pessoaNome,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = reg.pessoaTipo.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.outline)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (!reg.veiculoPlaca.isNullOrBlank()) "Veículo: ${reg.veiculoPlaca}" else "Acesso Pedestre",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = reg.dataHora,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.outline)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = if (isAutorizado) reg.tipoEvento.uppercase() else "NEGADO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBg)
                        .border(1.dp, badgeColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}