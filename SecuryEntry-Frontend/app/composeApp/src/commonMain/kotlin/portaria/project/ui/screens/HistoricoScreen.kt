package portaria.project.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import portaria.project.navigation.AppNavigator
import portaria.project.ui.components.SaaSLayout
import portaria.project.ui.viewmodels.HistoricoViewModel

// Auditoria e histórico de acessos (HistoricoListScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoListScreen(navController: AppNavigator, viewModel: HistoricoViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var busca by remember { mutableStateOf("") }

    val filtrada = uiState.registros.filter { reg ->
        (reg.pessoaNome?.contains(busca, ignoreCase = true) == true) ||
                (reg.pessoaTipo?.contains(busca, ignoreCase = true) == true) ||
                (reg.veiculoPlaca?.contains(busca, ignoreCase = true) == true)
    }

    SaaSLayout(title = "Auditoria de Acessos", navigator = navController) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = { Text("Pesquisar por nome, tipo ou placa...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                )
            )

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text(
                    text = "${filtrada.size} evento(s) registrado(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtrada) { reg ->
                        val isEntrada = reg.tipoEvento == "ENTRADA"
                        val isAutorizado = reg.status == "AUTORIZADO"

                        val iconColor = if (!isAutorizado) Color(0xFFEF4444) else if (isEntrada) Color(0xFF10B981) else Color(0xFF94A3B8)
                        val mainIcon = if (!isAutorizado) Icons.Default.Close else if (isEntrada) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp
                        val iconBg = if (!isAutorizado) Color(0xFFEF4444).copy(alpha = 0.08f) else if (isEntrada) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFF94A3B8).copy(alpha = 0.08f)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(iconBg)
                                        .border(1.dp, iconColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(mainIcon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = reg.pessoaNome ?: "Desconhecido",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = (reg.pessoaTipo ?: "N/A").uppercase(),
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
                                        "${if(!reg.veiculoPlaca.isNullOrBlank()) "Veículo: ${reg.veiculoPlaca}" else "Acesso Pedestre"}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (!reg.observacao.isNullOrBlank()) {
                                        Text(
                                            reg.observacao ?: "",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = reg.dataHora ?: "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = if (isAutorizado) reg.tipoEvento ?: "" else "ACESSO NEGADO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = iconColor,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(iconBg)
                                            .border(1.dp, iconColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}