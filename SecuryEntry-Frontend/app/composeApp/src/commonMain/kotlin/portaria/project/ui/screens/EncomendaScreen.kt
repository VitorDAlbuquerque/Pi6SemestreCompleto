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
import portaria.project.ui.components.SaaSLayout
import portaria.project.ui.viewmodels.EncomendaViewModel
import portaria.project.navigation.AppRoute
import portaria.project.data.models.Apartment
import portaria.project.data.repository.ApartmentRepository

// Quadro de acompanhamento de encomendas (EncomendaListScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncomendaListScreen(navController: AppNavigator, viewModel: EncomendaViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = LocalCurrentUser.current
    var busca by remember { mutableStateOf("") }

    val aptRepository = remember { ApartmentRepository() }
    var apartamentos by remember { mutableStateOf<List<Apartment>>(emptyList()) }
    LaunchedEffect(Unit) {
        aptRepository.getAll().onSuccess { apartamentos = it }
    }

    val filtrada = uiState.encomendas.filter {
        it.description.contains(busca, ignoreCase = true) ||
                it.recipientName.contains(busca, ignoreCase = true)
    }

    SaaSLayout(title = "Gestão de Encomendas", navigator = navController) { padding ->
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
                    placeholder = { Text("Pesquisar por descrição ou destinatário...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                    onClick = { navController.navigate(AppRoute.EncomendasForm()) },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("NOVA ENCOMENDA", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text(
                    text = "${filtrada.size} encomenda(s) registrada(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtrada) { enc ->
                        val isPendente = !enc.deliveredToResident
                        val statusText = if (isPendente) "PENDENTE" else "ENTREGUE"
                        val badgeColor = if (isPendente) Color(0xFFF59E0B) else Color(0xFF10B981)
                        val badgeBg = if (isPendente) Color(0xFFF59E0B).copy(alpha = 0.08f) else Color(0xFF10B981).copy(alpha = 0.08f)

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
                                        .background(badgeBg)
                                        .border(1.dp, badgeColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPendente) Icons.Default.Email else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = enc.description,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = statusText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = badgeColor,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(badgeBg)
                                                .border(1.dp, badgeColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    val aptNum = remember(enc.apartmentId, apartamentos) {
                                        apartamentos.find { it.id == enc.apartmentId }?.number ?: enc.apartmentId
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Destinatário: ${enc.recipientName}  •  Unidade: $aptNum",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (!enc.storageLocation.isNullOrBlank()) {
                                        Text(
                                            "Local: ${enc.storageLocation}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                    if (!isPendente && !enc.notes.isNullOrBlank()) {
                                        Text(
                                            "Retirado por: ${enc.notes}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { navController.navigate(AppRoute.EncomendasForm(id = enc.id ?: "")) }) {
                                        Icon(Icons.Default.EditNote, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteEncomenda(enc.id ?: "", currentUser) }) {
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

// Registro e controle de entregas (EncomendaFormScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncomendaFormScreen(navController: AppNavigator, viewModel: EncomendaViewModel, encomendaId: String = "") {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = LocalCurrentUser.current
    val form = uiState.form
    val isEditing = encomendaId.isNotBlank()

    val aptRepository = remember { ApartmentRepository() }
    var apartamentos by remember { mutableStateOf<List<Apartment>>(emptyList()) }
    var aptDropdownAberto by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        aptRepository.getAll().onSuccess { apartamentos = it }
    }

    LaunchedEffect(encomendaId) { viewModel.prepareEncomendaForm(encomendaId) }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditing) "EDITAR ENCOMENDA" else "REGISTRAR ENCOMENDA", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp) },
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
                    .width(540.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(28.dp)
                ) {
                    Text(
                        "DADOS DE RECEBIMENTO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = form.description,
                        onValueChange = { viewModel.setEncomendaForm { f -> f.copy(description = it) } },
                        label = { Text("DESCRIÇÃO DO PACOTE") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = form.recipientName,
                        onValueChange = { viewModel.setEncomendaForm { f -> f.copy(recipientName = it) } },
                        label = { Text("NOME DO DESTINATÁRIO") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = aptDropdownAberto,
                        onExpandedChange = { aptDropdownAberto = it }
                    ) {
                        OutlinedTextField(
                            value = form.apartmentId,
                            onValueChange = { newValue ->
                                viewModel.setEncomendaForm { it.copy(apartmentId = newValue) }
                                aptDropdownAberto = true
                            },
                            label = { Text("ID DA UNIDADE (DESTINO)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aptDropdownAberto) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        val filtrados = remember(form.apartmentId, apartamentos) {
                            if (form.apartmentId.isBlank()) apartamentos
                            else apartamentos.filter {
                                it.number.contains(form.apartmentId, ignoreCase = true) ||
                                        it.block.contains(form.apartmentId, ignoreCase = true)
                            }
                        }

                        if (filtrados.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = aptDropdownAberto,
                                onDismissRequest = { aptDropdownAberto = false }
                            ) {
                                filtrados.forEach { apt ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("Apto ${apt.number} — Bloco ${apt.block}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("Andar ${apt.floor}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setEncomendaForm { it.copy(apartmentId = apt.id ?: "") }
                                            aptDropdownAberto = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = form.storageLocation,
                        onValueChange = { viewModel.setEncomendaForm { f -> f.copy(storageLocation = it) } },
                        label = { Text("LOCAL DE ARMAZENAMENTO (EX: PRATELEIRA A)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
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
                                Text("ENCOMENDA ENTREGUE", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                Text("Marque se o morador já retirou o pacote na portaria.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = form.deliveredToResident,
                                onCheckedChange = { v -> viewModel.setEncomendaForm { f -> f.copy(deliveredToResident = v) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    if (form.deliveredToResident) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = form.notes,
                            onValueChange = { viewModel.setEncomendaForm { f -> f.copy(notes = it) } },
                            label = { Text("NOME DA PESSOA QUE RETIROU") },
                            placeholder = { Text("Ex: João Silva (Morador/Cônjuge)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    if (uiState.error != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.submitEncomenda(encomendaId, currentUser) },
                        enabled = !uiState.isLoading && form.description.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("GRAVAR ENCOMENDA", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}