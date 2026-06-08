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
import portaria.project.navigation.AppNavigator
import portaria.project.ui.components.SaaSLayout
import portaria.project.ui.viewmodels.PrestadorViewModel
import portaria.project.navigation.AppRoute

// Listagem de prestadores de serviço (PrestadorListScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestadorListScreen(navController: AppNavigator, viewModel: PrestadorViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var busca by remember { mutableStateOf("") }

    val filtrada = uiState.prestadores.filter {
        it.companyName.contains(busca, ignoreCase = true) ||
                it.employeeName.contains(busca, ignoreCase = true)
    }

    SaaSLayout(title = "Controle de Prestadores", navigator = navController) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            // Search and CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Pesquisar por empresa ou funcionário...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    )
                )
                Button(
                    onClick = { navController.navigate(AppRoute.PrestadoresForm()) },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("NOVO PRESTADOR", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text(
                    text = "${filtrada.size} registro(s) encontrado(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtrada) { p ->
                        val statusColor = if (p.isActive) Color(0xFF10B981) else Color(0xFFEF4444)
                        val statusBg = if (p.isActive) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFFEF4444).copy(alpha = 0.08f)

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
                                        .background(statusBg)
                                        .border(1.dp, statusColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = p.companyName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (p.isActive) "ATIVO" else "BLOQUEADO",
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
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Funcionário: ${p.employeeName}  •  Serviço: ${p.serviceType}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (p.isActive) "Horário permitido: ${p.allowedStartTime} - ${p.allowedEndTime}" else "Acesso suspenso na portaria",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { navController.navigate(AppRoute.PrestadoresForm(id = p.id ?: "")) }) {
                                        Icon(Icons.Default.EditNote, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deletePrestador(p.id ?: "") }) {
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

// Formulário de cadastro e controle de acesso de prestadores (PrestadorFormScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestadorFormScreen(navController: AppNavigator, viewModel: PrestadorViewModel, prestadorId: String = "") {
    val uiState by viewModel.uiState.collectAsState()
    val form = uiState.form
    val isEditing = prestadorId.isNotBlank()

    LaunchedEffect(prestadorId) { viewModel.preparePrestadorForm(prestadorId) }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditing) "EDITAR PRESTADOR" else "REGISTRAR PRESTADOR", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .width(580.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(28.dp)
                ) {
                    Text(
                        "DADOS DA EMPRESA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = form.companyName,
                        onValueChange = { viewModel.setPrestadorForm { f -> f.copy(companyName = it) } },
                        label = { Text("NOME DA EMPRESA") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = form.serviceType,
                        onValueChange = { viewModel.setPrestadorForm { f -> f.copy(serviceType = it) } },
                        label = { Text("TIPO DE SERVIÇO (EX: INTERNET, LIMPEZA, OBRAS)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    Spacer(Modifier.height(20.dp))

                    Text(
                        "DADOS DO FUNCIONÁRIO AUTORIZADO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = form.employeeName,
                        onValueChange = { viewModel.setPrestadorForm { f -> f.copy(employeeName = it) } },
                        label = { Text("NOME DO FUNCIONÁRIO") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = form.cpf,
                            onValueChange = { viewModel.setPrestadorForm { f -> f.copy(cpf = it) } },
                            label = { Text("CPF") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        OutlinedTextField(
                            value = form.phone,
                            onValueChange = { viewModel.setPrestadorForm { f -> f.copy(phone = it) } },
                            label = { Text("TELEFONE") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    Spacer(Modifier.height(20.dp))

                    Text(
                        "REGRAS DE ACESSO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = form.allowedStartTime,
                            onValueChange = { viewModel.setPrestadorForm { f -> f.copy(allowedStartTime = it) } },
                            label = { Text("HORÁRIO INÍCIO (EX: 08:00)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        OutlinedTextField(
                            value = form.allowedEndTime,
                            onValueChange = { viewModel.setPrestadorForm { f -> f.copy(allowedEndTime = it) } },
                            label = { Text("HORÁRIO FIM (EX: 18:00)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    Spacer(Modifier.height(24.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("AUTORIZAÇÃO ATIVA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                Text("Desative para bloquear a entrada deste prestador temporariamente.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = form.isActive,
                                onCheckedChange = { v -> viewModel.setPrestadorForm { f -> f.copy(isActive = v) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    if (uiState.error != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.submitPrestador(prestadorId) },
                        enabled = !uiState.isLoading && form.companyName.isNotBlank() && form.employeeName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("GRAVAR PRESTADOR", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}