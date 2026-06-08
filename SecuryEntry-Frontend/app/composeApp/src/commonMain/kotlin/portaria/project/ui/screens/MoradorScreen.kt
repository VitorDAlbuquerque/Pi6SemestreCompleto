package portaria.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import portaria.project.navigation.AppNavigator
import portaria.project.navigation.AppRoute
import portaria.project.ui.components.SaaSLayout
import portaria.project.ui.viewmodels.MoradorViewModel
import portaria.project.utils.cpfValido
import portaria.project.utils.emailValido
import portaria.project.utils.senhaValida

// Diretório de listagem de moradores
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoradorListScreen(navController: AppNavigator, viewModel: MoradorViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var busca by remember { mutableStateOf("") }

    val aptRepository = remember { portaria.project.data.repository.ApartmentRepository() }
    var apartamentos by remember { mutableStateOf<List<portaria.project.data.models.Apartment>>(emptyList()) }
    LaunchedEffect(Unit) {
        aptRepository.getAll().onSuccess { apartamentos = it }
    }

    val filtrada = uiState.moradores.filter {
        it.name.contains(busca, ignoreCase = true) ||
                (it.apartmentId ?: "").contains(busca, ignoreCase = true)
    }

    SaaSLayout(title = "Gestão de Moradores", navigator = navController) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header / Search Bar & CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Pesquisar morador ou apartamento...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                    onClick = { navController.navigate(AppRoute.MoradoresForm()) },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("NOVO MORADOR", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Text(
                    text = "${filtrada.size} morador(es) encontrado(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtrada) { m ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = m.name.take(2).uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = m.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = (m.residentType ?: "MORADOR").uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                        if (m.isActive == false) {
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = "INATIVO",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    val aptNum = remember(m.apartmentId, apartamentos) {
                                        apartamentos.find { it.id == m.apartmentId }?.number ?: m.apartmentId ?: "N/A"
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "APTO: $aptNum  •  BLOCO: ${m.block ?: "N/A"}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { navController.navigate(AppRoute.MoradoresForm(id = m.id ?: "")) }
                                    ) {
                                        Icon(Icons.Default.EditNote, contentDescription = "Editar Morador", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteMorador(m.id ?: "") }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar Morador", tint = MaterialTheme.colorScheme.error)
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

// Formulário de cadastro e atualização de moradores
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoradorFormScreen(navController: AppNavigator, viewModel: MoradorViewModel, moradorId: String = "") {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = moradorId.isNotBlank()
    val form = uiState.form

    val tipos = listOf("PROPRIETARIO", "INQUILINO", "DEPENDENTE", "ADMIN")

    val aptRepository = remember { portaria.project.data.repository.ApartmentRepository() }
    var apartamentos by remember { mutableStateOf<List<portaria.project.data.models.Apartment>>(emptyList()) }
    var aptDropdownAberto by remember { mutableStateOf(false) }

    val emailError = remember(form.email) {
        if (form.email.isNotBlank() && !emailValido(form.email)) "Insira um e-mail válido." else null
    }
    val senhaError = remember(form.senha) {
        if (form.senha.isNotBlank() && !senhaValida(form.senha))
            "A senha deve ter ao menos 8 caracteres, uma letra e um número." else null
    }
    val cpfError = remember(form.cpf) {
        if (form.cpf.isNotBlank() && !cpfValido(form.cpf)) "CPF inválido. Verifique o número digitado." else null
    }

    val aptSelecionado = remember(form.selectedApartmentId, apartamentos) {
        apartamentos.find { it.id == form.selectedApartmentId }
    }

    LaunchedEffect(Unit) {
        aptRepository.getAll().onSuccess { apartamentos = it }
    }

    LaunchedEffect(moradorId) {
        viewModel.prepareMoradorForm(moradorId)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            viewModel.clearMoradorSelecionado()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditing) "EDITAR MORADOR" else "REGISTRO DE MORADOR", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    Text(
                        "DADOS OPERACIONAIS E ACESSO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = form.nome,
                        onValueChange = { v -> viewModel.setMoradorForm { f -> f.copy(nome = v) } },
                        label = { Text("NOME COMPLETO") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = form.email,
                        onValueChange = { v -> viewModel.setMoradorForm { f -> f.copy(email = v) } },
                        label = { Text("E-MAIL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = form.senha,
                        onValueChange = { v -> viewModel.setMoradorForm { f -> f.copy(senha = v) } },
                        label = { Text("SENHA") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = senhaError != null,
                        supportingText = senhaError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = form.cpf,
                            onValueChange = { v -> viewModel.setMoradorForm { f -> f.copy(cpf = v) } },
                            label = { Text("CPF") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            isError = cpfError != null,
                            supportingText = cpfError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        OutlinedTextField(
                            value = form.telefone,
                            onValueChange = { v -> viewModel.setMoradorForm { f -> f.copy(telefone = v) } },
                            label = { Text("TELEFONE") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = form.nascimento,
                        onValueChange = { v -> viewModel.setMoradorForm { f -> f.copy(nascimento = v) } },
                        label = { Text("DATA DE NASCIMENTO (DD/MM/AAAA)") },
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
                        "VÍNCULO FÍSICO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    ExposedDropdownMenuBox(expanded = aptDropdownAberto, onExpandedChange = { aptDropdownAberto = it }) {
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
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        ExposedDropdownMenu(expanded = aptDropdownAberto, onDismissRequest = { aptDropdownAberto = false }) {
                            DropdownMenuItem(
                                text = { Text("Nenhum") },
                                onClick = {
                                    viewModel.setMoradorForm { it.copy(selectedApartmentId = null) }
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
                                        viewModel.setMoradorForm { it.copy(selectedApartmentId = apt.id) }
                                        aptDropdownAberto = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    Spacer(Modifier.height(20.dp))

                    Text(
                        "TIPO DE MORADOR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tipos.forEach { tipo ->
                            FilterChip(
                                selected = form.tipoMorador == tipo,
                                onClick = { viewModel.setMoradorForm { it.copy(tipoMorador = tipo) } },
                                label = { Text(tipo, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = form.tipoMorador == tipo,
                                    borderColor = MaterialTheme.colorScheme.outline,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.5.dp
                                )
                            )
                        }
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
                                Text("ACESSO ATIVO", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "Permite ao utilizador entrar no sistema e passar na portaria",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = form.ativo,
                                onCheckedChange = { v -> viewModel.setMoradorForm { f -> f.copy(ativo = v) } },
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (isEditing) viewModel.carregarMorador(moradorId)
                                else viewModel.clearMoradorForm()
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text("LIMPAR", fontWeight = FontWeight.Bold) }

                        Button(
                            onClick = { viewModel.submitMorador(moradorId) },
                            enabled = !uiState.isLoading && emailError == null && senhaError == null && cpfError == null,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("GRAVAR REGISTRO", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}