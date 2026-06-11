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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import portaria.project.LocalCurrentUser
import portaria.project.navigation.AppNavigator
import portaria.project.ui.components.SaaSLayout
import portaria.project.ui.viewmodels.VeiculoViewModel
import portaria.project.navigation.AppRoute

// Quadro de veículos e placas registradas (VeiculoListScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeiculoListScreen(navController: AppNavigator, viewModel: VeiculoViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = LocalCurrentUser.current
    var busca by remember { mutableStateOf("") }

    val aptRepository = remember { portaria.project.data.repository.ApartmentRepository() }
    var apartamentos by remember { mutableStateOf<List<portaria.project.data.models.Apartment>>(emptyList()) }
    LaunchedEffect(Unit) {
        aptRepository.getAll().onSuccess { apartamentos = it }
    }

    val filtrada = uiState.veiculos.filter {
        it.plate.contains(busca, ignoreCase = true) ||
                it.model.contains(busca, ignoreCase = true)
    }

    SaaSLayout(title = "Veículos", navigator = navController) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            // Busca e Ações
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Pesquisar por placa ou modelo...") },
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
                    onClick = { navController.navigate(AppRoute.VeiculosForm()) },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("NOVO VEÍCULO", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
                }
            } else {
                if (filtrada.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Nenhum veículo registrado",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "${filtrada.size} veículo(s) registrado(s)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filtrada) { v ->
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
                                    // Veículo Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = v.plate.uppercase(),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            val aptNum = remember(v.apartmentId, apartamentos) {
                                                apartamentos.find { it.id == v.apartmentId }?.number ?: v.apartmentId
                                            }
                                            Text(
                                                text = "Apto: $aptNum",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.outline)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            text = "${v.model}  •  Cor: ${v.color}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Ações
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(
                                            onClick = { navController.navigate(AppRoute.VeiculosForm(id = v.id ?: "")) },
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
                                            onClick = { viewModel.deleteVeiculo(v.id ?: "", currentUser) },
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

// Registro e identificação de veículos (VeiculoFormScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeiculoFormScreen(navController: AppNavigator, viewModel: VeiculoViewModel, veiculoId: String = "", fixedApartmentId: String? = null) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = LocalCurrentUser.current
    val form = uiState.form
    val isEditing = veiculoId.isNotBlank()

    val aptRepository = remember { portaria.project.data.repository.ApartmentRepository() }
    var apartamentos by remember { mutableStateOf<List<portaria.project.data.models.Apartment>>(emptyList()) }
    var aptDropdownAberto by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        aptRepository.getAll().onSuccess { apartamentos = it }
    }

    val aptSelecionado = remember(form.selectedApartmentId, apartamentos) {
        apartamentos.find { it.id == form.selectedApartmentId }
    }

    LaunchedEffect(veiculoId) { viewModel.prepareVeiculoForm(veiculoId, fixedApartmentId) }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Veículo" else "Novo Veículo",
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
                    .width(520.dp)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cartão do Formulário
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
                            text = "Dados do Veículo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = form.placa,
                            onValueChange = { viewModel.setVeiculoForm { f -> f.copy(placa = it.uppercase()) } },
                            label = { Text("PLACA DO VEÍCULO") },
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

                        if (fixedApartmentId == null) {
                            ExposedDropdownMenuBox(
                                expanded = aptDropdownAberto,
                                onExpandedChange = { aptDropdownAberto = it }
                            ) {
                                OutlinedTextField(
                                    value = aptSelecionado?.let { "Apto ${it.number} — Bloco ${it.block}" } ?: "Nenhum apartamento selecionado",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("SELECIONAR APARTAMENTO") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aptDropdownAberto) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
                                            viewModel.setVeiculoForm { it.copy(selectedApartmentId = null) }
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
                                                viewModel.setVeiculoForm { it.copy(selectedApartmentId = apt.id) }
                                                aptDropdownAberto = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = form.modelo,
                                onValueChange = { viewModel.setVeiculoForm { f -> f.copy(modelo = it) } },
                                label = { Text("MODELO") },
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
                                value = form.cor,
                                onValueChange = { viewModel.setVeiculoForm { f -> f.copy(cor = it) } },
                                label = { Text("COR") },
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

                        if (uiState.error != null) {
                            Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.submitVeiculo(veiculoId, fixedApartmentId, currentUser) },
                            enabled = !uiState.isLoading && form.placa.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
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
}