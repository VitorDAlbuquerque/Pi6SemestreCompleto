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
import kotlinx.coroutines.launch
import portaria.project.data.api.RepositorioRemoto
import portaria.project.data.models.LprResult
import portaria.project.navigation.AppNavigator
import portaria.project.navigation.AppRoute
import portaria.project.ui.components.SaaSLayout
import portaria.project.ui.viewmodels.UsuarioViewModel

// Listagem de colaboradores do sistema (UsuariosListScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosListScreen(navController: AppNavigator, viewModel: UsuarioViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var busca by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val repositorio = remember { RepositorioRemoto() }

    var isLprLoading by remember { mutableStateOf(false) }
    var lprResult by remember { mutableStateOf<LprResult?>(null) }

    val filtrada = uiState.usuarios.filter {
        (it.name?.contains(busca, ignoreCase = true) == true) ||
                (it.email?.contains(busca, ignoreCase = true) == true)
    }

    if (lprResult != null) {
        LprResultDialog(
            result = lprResult!!,
            onDismiss = { lprResult = null }
        )
    }

    SaaSLayout(title = "Gestão de Colaboradores", navigator = navController) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            // Search & CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Pesquisar nome ou e-mail...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                    onClick = {
                        scope.launch {
                            isLprLoading = true
                            runCatching { repositorio.triggerLpr() }
                                .onSuccess { lprResult = it }
                                .onFailure { e ->
                                    lprResult = LprResult(
                                        status = "error",
                                        message = e.message ?: "Erro ao conectar com o servidor LPR."
                                    )
                                }
                            isLprLoading = false
                        }
                    },
                    enabled = !isLprLoading,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F766E)
                    )
                ) {
                    if (isLprLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("LENDO CÂMERA…", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    } else {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("INICIAR LPR", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
                Button(
                    onClick = { navController.navigate(AppRoute.UsuariosForm()) },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("NOVO COLABORADOR", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text(
                    text = "${filtrada.size} colaborador(es) do sistema",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtrada) { u ->
                        val roleText = (u.role ?: "OPERADOR").uppercase().trim()
                        val isAdm = roleText == "ADMIN"
                        val roleColor = if (isAdm) MaterialTheme.colorScheme.primary else Color(0xFF8B5CF6)
                        val roleBg = if (isAdm) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color(0xFF8B5CF6).copy(alpha = 0.08f)

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
                                        .background(roleBg)
                                        .border(1.dp, roleColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (u.name ?: "CO").take(2).uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = roleColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = u.name ?: "Sem Nome",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = roleText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = roleColor,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(roleBg)
                                                .border(1.dp, roleColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                        if (u.isActive == false) {
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
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = u.email ?: "sem-email@dominio.com",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { navController.navigate(AppRoute.UsuariosForm(id = u.id ?: "")) }) {
                                        Icon(Icons.Default.EditNote, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteUsuario(u.id ?: "") }) {
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

// Formulário de cadastro e definição de permissões de colaboradores (UsuariosFormScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosFormScreen(navController: AppNavigator, viewModel: UsuarioViewModel, userId: String = "") {
    val uiState by viewModel.uiState.collectAsState()
    val form = uiState.form
    val isEditing = userId.isNotBlank()

    LaunchedEffect(userId) { viewModel.prepareUsuarioForm(userId) }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditing) "EDITAR COLABORADOR" else "CRIAR COLABORADOR", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp) },
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
                        "CREDENCIAIS DE ACESSO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = form.name,
                        onValueChange = { viewModel.setUsuarioForm { f -> f.copy(name = it) } },
                        label = { Text("NOME DO COLABORADOR") },
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
                        onValueChange = { viewModel.setUsuarioForm { f -> f.copy(email = it) } },
                        label = { Text("E-MAIL DE LOGIN") },
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
                        "PERMISSÕES (ROLE)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    val perfis = listOf("ADMIN", "GERENTE", "PORTEIRO")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        perfis.forEach { perfil ->
                            FilterChip(
                                selected = form.role == perfil,
                                onClick = { viewModel.setUsuarioForm { it.copy(role = perfil) } },
                                label = { Text(perfil, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = form.role == perfil,
                                    borderColor = MaterialTheme.colorScheme.outline,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.5.dp
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    Spacer(Modifier.height(20.dp))

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
                                Text("CONTA ATIVA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                Text("Permite ao colaborador efetuar login no painel administrativo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = form.isActive,
                                onCheckedChange = { v -> viewModel.setUsuarioForm { f -> f.copy(isActive = v) } },
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
                        onClick = { viewModel.submitUsuario(userId) },
                        enabled = !uiState.isLoading && form.name.isNotBlank() && form.email.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("SALVAR CONTA", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// Dialog de resultado do reconhecimento de placa (LprResultDialog)
@Composable
private fun LprResultDialog(result: LprResult, onDismiss: () -> Unit) {
    val isOpened = result.status == "gate_opened"
    val isDenied = result.status == "denied"
    val isError = result.status == "error"
    val isNoPlate = result.status == "no_plate"

    val accentColor = when {
        isOpened  -> Color(0xFF10B981)
        isDenied  -> Color(0xFFEF4444)
        isNoPlate -> Color(0xFFF59E0B)
        else      -> Color(0xFF94A3B8)
    }

    val icon = when {
        isOpened  -> Icons.Default.CheckCircle
        isDenied  -> Icons.Default.Cancel
        isNoPlate -> Icons.Default.Warning
        else      -> Icons.Default.Error
    }

    val title = when {
        isOpened  -> "Portão Aberto!"
        isDenied  -> "Acesso Negado"
        isNoPlate -> "Placa Não Detectada"
        else      -> "Erro LPR"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(28.dp))
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = accentColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = result.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!result.plate.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.07f))
                            .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, null, tint = accentColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = result.plate!!,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = accentColor,
                            letterSpacing = 2.sp
                        )
                    }
                }
                if (!result.apartment.isNullOrBlank()) {
                    Text(
                        text = "Apto vinculado: ${result.apartment}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    )
}