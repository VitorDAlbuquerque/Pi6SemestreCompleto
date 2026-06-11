package portaria.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import portaria.project.LocalCurrentUser
import portaria.project.navigation.AppNavigator
import portaria.project.navigation.AppRoute
import portaria.project.ui.components.SaaSLayout
import portaria.project.ui.viewmodels.OcorrenciaViewModel

@Composable
fun OcorrenciaListScreen(navController: AppNavigator, viewModel: OcorrenciaViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = LocalCurrentUser.current
    var busca by remember { mutableStateOf("") }

    val filtrada = uiState.ocorrencias.filter {
        it.title.contains(busca, ignoreCase = true) ||
                it.category.contains(busca, ignoreCase = true) ||
                it.reportedBy.contains(busca, ignoreCase = true)
    }

    SaaSLayout(title = "Ocorrencias", navigator = navController) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Pesquisar por titulo, categoria ou responsavel...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { navController.navigate(AppRoute.OcorrenciasForm()) },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("NOVA OCORRENCIA", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text(
                    text = "${filtrada.size} ocorrencia(s) registrada(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtrada) { ocorrencia ->
                        val severity = ocorrencia.severity.uppercase()
                        val status = ocorrencia.status.uppercase()
                        val severityColor = when (severity) {
                            "ALTA" -> Color(0xFFEF4444)
                            "BAIXA" -> Color(0xFF10B981)
                            else -> Color(0xFFF59E0B)
                        }
                        val statusColor = if (status == "RESOLVIDA") Color(0xFF10B981) else severityColor
                        val statusBg = statusColor.copy(alpha = 0.08f)

                        Card(
                            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(statusBg)
                                        .border(1.dp, statusColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = statusColor)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(ocorrencia.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = status,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = statusColor,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(statusBg)
                                                .border(1.dp, statusColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${ocorrencia.category}  •  Prioridade: $severity  •  Responsavel: ${ocorrencia.reportedBy}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        ocorrencia.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        maxLines = 2
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { navController.navigate(AppRoute.OcorrenciasForm(id = ocorrencia.id ?: "")) }) {
                                        Icon(Icons.Default.EditNote, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteOcorrencia(ocorrencia.id ?: "", currentUser) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcorrenciaFormScreen(navController: AppNavigator, viewModel: OcorrenciaViewModel, ocorrenciaId: String = "") {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = LocalCurrentUser.current
    val form = uiState.form
    val isEditing = ocorrenciaId.isNotBlank()

    LaunchedEffect(ocorrenciaId, currentUser) { viewModel.prepareOcorrenciaForm(ocorrenciaId, currentUser) }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditing) "EDITAR OCORRENCIA" else "REGISTRAR OCORRENCIA", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.widthIn(max = 620.dp).fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(28.dp)) {
                    Text("DADOS DA OCORRENCIA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 20.dp))

                    OutlinedTextField(form.title, { viewModel.setOcorrenciaForm { f -> f.copy(title = it) } }, label = { Text("TITULO") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(form.description, { viewModel.setOcorrenciaForm { f -> f.copy(description = it) } }, label = { Text("DESCRICAO") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(8.dp))
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(form.category, { viewModel.setOcorrenciaForm { f -> f.copy(category = it) } }, label = { Text("CATEGORIA") }, placeholder = { Text("SEGURANCA, MANUTENCAO...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(form.severity, { viewModel.setOcorrenciaForm { f -> f.copy(severity = it) } }, label = { Text("PRIORIDADE") }, placeholder = { Text("BAIXA, MEDIA, ALTA") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(form.status, { viewModel.setOcorrenciaForm { f -> f.copy(status = it) } }, label = { Text("STATUS") }, placeholder = { Text("ABERTA, EM_ANDAMENTO, RESOLVIDA") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(form.apartmentId, { viewModel.setOcorrenciaForm { f -> f.copy(apartmentId = it) } }, label = { Text("ID DA UNIDADE") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(form.reportedBy, { viewModel.setOcorrenciaForm { f -> f.copy(reportedBy = it) } }, label = { Text("REGISTRADO POR") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(form.createdAt, { viewModel.setOcorrenciaForm { f -> f.copy(createdAt = it) } }, label = { Text("DATA DE ABERTURA") }, placeholder = { Text("Ex: 2026-06-09 14:30") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(form.resolvedAt, { viewModel.setOcorrenciaForm { f -> f.copy(resolvedAt = it) } }, label = { Text("DATA DE RESOLUCAO") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(form.notes, { viewModel.setOcorrenciaForm { f -> f.copy(notes = it) } }, label = { Text("OBSERVACOES INTERNAS") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(8.dp))

                    uiState.error?.let {
                        Spacer(Modifier.height(16.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.submitOcorrencia(ocorrenciaId, currentUser) },
                        enabled = !uiState.isLoading && form.title.isNotBlank() && form.description.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("GRAVAR OCORRENCIA", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
