package portaria.project.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import portaria.project.data.repository.ApartmentRepository
import portaria.project.ui.components.SaaSLayout
import portaria.project.ui.viewmodels.VisitanteViewModel
import portaria.project.navigation.AppRoute

// Listagem de visitantes registrados (VisitanteListScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitanteListScreen(navController: AppNavigator, viewModel: VisitanteViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = LocalCurrentUser.current
    var busca by remember { mutableStateOf("") }

    val aptRepository = remember { ApartmentRepository() }
    var apartamentos by remember { mutableStateOf<List<portaria.project.data.models.Apartment>>(emptyList()) }
    LaunchedEffect(Unit) {
        aptRepository.getAll().onSuccess { apartamentos = it }
    }

    val filtrada = uiState.visitantes.filter {
        it.name.contains(busca, ignoreCase = true) ||
                it.cpf.contains(busca, ignoreCase = true)
    }

    SaaSLayout(title = "Visitantes", navigator = navController) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Cabeçalho e Ações
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Pesquisar por nome ou CPF...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                    ),
                    singleLine = true
                )

                Button(
                    onClick = { navController.navigate(AppRoute.VisitantesForm()) },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("NOVO VISITANTE", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
                }
            } else {
                if (filtrada.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Nenhum visitante registrado",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "${filtrada.size} visitante(s) registrado(s)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filtrada) { item ->
                            val isLivre = item.freeAccess
                            val badgeColor = if (isLivre) Color(0xFF10B981) else Color(0xFF6366F1)
                            val badgeBg = if (isLivre) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFF6366F1).copy(alpha = 0.08f)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor.copy(alpha = 0.1f))
                                            .border(1.dp, badgeColor.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.name.take(2).uppercase(),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = badgeColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.width(8.dp))

                                            // Badge de Acesso
                                            Text(
                                                text = if (isLivre) "ACESSO LIVRE" else "AGENDADO",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = badgeColor,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(badgeBg)
                                                    .border(1.dp, badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val aptNum = remember(item.apartmentId, apartamentos) {
                                            apartamentos.find { it.id == item.apartmentId }?.number ?: item.apartmentId.takeIf { it.isNotBlank() } ?: "N/A"
                                        }
                                        Text(
                                            text = "CPF: ${item.cpf}  •  Tel: ${item.phone}  •  Apto: $aptNum",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Ações
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(
                                            onClick = { navController.navigate(AppRoute.VisitantesForm(id = item.id ?: "")) },
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EditNote,
                                                contentDescription = "Editar",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteVisitante(item.id ?: "", currentUser) },
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Excluir",
                                                tint = MaterialTheme.colorScheme.error
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
    }
}

// Formulário de cadastro/edição de visitantes (VisitanteFormScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitanteFormScreen(
    navController: AppNavigator,
    viewModel: VisitanteViewModel,
    visitanteId: String = "",
    fixedApartmentId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = LocalCurrentUser.current
    val isEditing = visitanteId.isNotBlank()
    val form = uiState.form

    val aptRepository = remember { ApartmentRepository() }
    var apartamentos by remember { mutableStateOf<List<portaria.project.data.models.Apartment>>(emptyList()) }
    var aptDropdownAberto by remember { mutableStateOf(false) }

    val aptSelecionado = remember(form.selectedApartmentId, apartamentos) {
        apartamentos.find { it.id == form.selectedApartmentId }
    }

    LaunchedEffect(Unit) {
        if (fixedApartmentId == null) {
            aptRepository.getAll().onSuccess { apartamentos = it }
        }
    }

    LaunchedEffect(visitanteId, fixedApartmentId) {
        viewModel.prepareVisitanteForm(visitanteId, fixedApartmentId)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            viewModel.clearVisitanteSelecionado()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Visitante" else "Novo Visitante",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .width(560.dp)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Cartão Formulário Principal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Informações do Visitante",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = form.nome,
                            onValueChange = { v -> viewModel.setVisitanteForm { f -> f.copy(nome = v) } },
                            label = { Text("NOME COMPLETO") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = form.cpf,
                                onValueChange = { v -> viewModel.setVisitanteForm { f -> f.copy(cpf = v) } },
                                label = { Text("CPF") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = form.telefone,
                                onValueChange = { v -> viewModel.setVisitanteForm { f -> f.copy(telefone = v) } },
                                label = { Text("TELEFONE") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = form.dataVisita,
                            onValueChange = { v -> viewModel.setVisitanteForm { f -> f.copy(dataVisita = v) } },
                            label = { Text("DATA DA VISITA (DD/MM/AAAA)") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = form.autorizadoPor,
                            onValueChange = { v -> viewModel.setVisitanteForm { f -> f.copy(autorizadoPor = v) } },
                            label = { Text("AUTORIZADO POR (NOME DO MORADOR)") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = form.notas,
                            onValueChange = { v -> viewModel.setVisitanteForm { f -> f.copy(notas = v) } },
                            label = { Text("OBSERVAÇÕES / NOTAS") },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }

                // Destino (Apenas se não for fixado pelo morador logado)
                if (fixedApartmentId == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Unidade de Destino",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            ExposedDropdownMenuBox(
                                expanded = aptDropdownAberto,
                                onExpandedChange = { aptDropdownAberto = it }
                            ) {
                                OutlinedTextField(
                                    value = aptSelecionado?.let { "Apartamento ${it.number} — Bloco ${it.block}" } ?: "Selecione o destino...",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("SELECIONAR APARTAMENTO") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aptDropdownAberto) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = aptDropdownAberto,
                                    onDismissRequest = { aptDropdownAberto = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Nenhum") },
                                        onClick = {
                                            viewModel.setVisitanteForm { it.copy(selectedApartmentId = null) }
                                            aptDropdownAberto = false
                                        }
                                    )
                                    apartamentos.forEach { apt ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text("Apto ${apt.number} — Bloco ${apt.block}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text("Andar ${apt.floor}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                viewModel.setVisitanteForm { it.copy(selectedApartmentId = apt.id) }
                                                aptDropdownAberto = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Permissões
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Configurações de Acesso",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Acesso Livre", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Autoriza entrada direta sem liberar novamente na portaria", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = form.acessoLivre,
                                onCheckedChange = { v -> viewModel.setVisitanteForm { f -> f.copy(acessoLivre = v) } }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Visita Ativa", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Indica se o agendamento ainda está válido e ativo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = form.ativo,
                                onCheckedChange = { v -> viewModel.setVisitanteForm { f -> f.copy(ativo = v) } }
                            )
                        }
                    }
                }

                if (uiState.error != null) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                // Botões de Ação
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isEditing) viewModel.carregarVisitante(visitanteId)
                            else viewModel.prepareVisitanteForm("", fixedApartmentId)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("RESTAURAR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { viewModel.submitVisitante(visitanteId, fixedApartmentId, currentUser) },
                        enabled = !uiState.isLoading && form.nome.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("GRAVAR REGISTRO", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}