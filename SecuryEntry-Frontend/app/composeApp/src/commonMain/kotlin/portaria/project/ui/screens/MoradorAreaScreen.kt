package portaria.project.ui.screens

// Importações e configuração inicial
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import portaria.project.navigation.AppNavigator
import portaria.project.navigation.AppRoute
import portaria.project.data.models.User
import portaria.project.ui.viewmodels.VisitanteViewModel
import portaria.project.ui.viewmodels.VeiculoViewModel
import portaria.project.ui.viewmodels.AuthViewModel
import portaria.project.ui.viewmodels.EncomendaViewModel

// Layout base do morador (Menu Lateral e TopBar)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoradorLayout(
    title: String,
    navigator: AppNavigator,
    currentUser: User?,
    authViewModel: AuthViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val aptRepository = remember { portaria.project.data.repository.ApartmentRepository() }
    var apartmentNumber by remember { mutableStateOf<String?>(null) }
    var apartmentBlock by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.apartmentId) {
        val aptId = currentUser?.apartmentId
        if (!aptId.isNullOrBlank()) {
            aptRepository.getById(aptId).onSuccess { apt ->
                apartmentNumber = apt.number
                apartmentBlock = apt.block
            }.onFailure {
                aptRepository.getAll().onSuccess { list ->
                    val resolved = list.find { it.id == aptId }
                        ?: list.find { it.number.equals(aptId, ignoreCase = true) }
                    if (resolved != null) {
                        apartmentNumber = resolved.number
                        apartmentBlock = resolved.block
                    } else {
                        apartmentNumber = aptId
                    }
                }.onFailure {
                    apartmentNumber = aptId
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(320.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 24.dp)
                ) {
                    // Header Perfil
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.name?.take(2)?.uppercase() ?: "MR",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentUser?.name ?: "Morador",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Apto ${apartmentNumber ?: currentUser?.apartmentId ?: "N/A"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))

                    // Menu Itens
                    Column(modifier = Modifier.weight(1f)) {
                        MoradorDrawerItem(
                            icon = Icons.Default.Home,
                            label = "Meu Painel",
                            isActive = title == "Painel do Morador"
                        ) {
                            scope.launch { drawerState.close() }
                            navigator.navigate(AppRoute.Home)
                        }

                        MoradorDrawerItem(
                            icon = Icons.Default.Person,
                            label = "Meu Perfil",
                            isActive = title == "Meu Perfil"
                        ) {
                            scope.launch { drawerState.close() }
                            navigator.navigate(AppRoute.MeuPerfil)
                        }

                        MoradorDrawerItem(
                            icon = Icons.Default.Domain,
                            label = "Meu Apartamento",
                            isActive = title == "Meu Apartamento"
                        ) {
                            scope.launch { drawerState.close() }
                            navigator.navigate(AppRoute.MeuApartamento)
                        }

                        MoradorDrawerItem(
                            icon = Icons.Default.Face,
                            label = "Minhas Visitas",
                            isActive = title == "Minhas Visitas"
                        ) {
                            scope.launch { drawerState.close() }
                            navigator.navigate(AppRoute.MinhasVisitas())
                        }

                        MoradorDrawerItem(
                            icon = Icons.Default.DirectionsCar,
                            label = "Meus Veículos",
                            isActive = title == "Meus Veículos"
                        ) {
                            scope.launch { drawerState.close() }
                            navigator.navigate(AppRoute.MeusVeiculos)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))

                    // Sair
                    MoradorDrawerItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        label = "Sair do Sistema",
                        isActive = false,
                        isError = true
                    ) {
                        scope.launch { drawerState.close() }
                        authViewModel.logout {
                            navigator.navigateReplaceRoot(AppRoute.Login)
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu Principal", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}

@Composable
private fun MoradorDrawerItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    isError: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    val contentColor = when {
        isError -> MaterialTheme.colorScheme.error
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(12.dp))
            }

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                color = contentColor,
                fontWeight = fontWeight,
                fontSize = 14.sp
            )
        }
    }
}

// Painel principal / Dashboard do morador
@Composable
fun MoradorDashboardScreen(
    navController: AppNavigator,
    currentUser: User?,
    authViewModel: AuthViewModel,
    encomendaViewModel: EncomendaViewModel
) {
    val encomendaState by encomendaViewModel.uiState.collectAsState()
    LaunchedEffect(currentUser) {
        encomendaViewModel.loadEncomendas(currentUser)
    }
    val pendentes = remember(encomendaState.encomendas) {
        encomendaState.encomendas.filter { !it.deliveredToResident }
    }

    MoradorLayout(title = "Painel do Morador", navigator = navController, currentUser = currentUser, authViewModel = authViewModel) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text(
                    text = "Acesso Rápido",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gerencie seus acessos, veículos e correspondências.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MoradorActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Autorizar Visita",
                    icon = Icons.Default.Face,
                    gradientStart = Color(0xFF3B82F6),
                    gradientEnd = Color(0xFF2563EB),
                    onClick = { navController.navigate(AppRoute.MinhasVisitas()) }
                )
                MoradorActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Meus Veículos",
                    icon = Icons.Default.DirectionsCar,
                    gradientStart = Color(0xFFF59E0B),
                    gradientEnd = Color(0xFFD97706),
                    onClick = { navController.navigate(AppRoute.MeusVeiculos) }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (pendentes.isNotEmpty()) Color(0xFFF59E0B).copy(alpha = 0.08f) else Color(0xFF10B981).copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (pendentes.isNotEmpty()) Icons.Default.MarkunreadMailbox else Icons.Default.Email,
                            contentDescription = null,
                            tint = if (pendentes.isNotEmpty()) Color(0xFFF59E0B) else Color(0xFF10B981),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Correspondências & Encomendas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (pendentes.isEmpty()) {
                            Text(
                                text = "Não há pacotes pendentes de retirada na portaria.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Você tem ${pendentes.size} pacote(s) pendente(s) de retirada na portaria:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                pendentes.forEach { enc ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = enc.description,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (!enc.storageLocation.isNullOrBlank()) {
                                                Text(
                                                    text = "Local: ${enc.storageLocation}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
fun MoradorActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    gradientStart: Color,
    gradientEnd: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(gradientStart, gradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Minhas visitas (Lista Filtrada)
@Composable
fun MinhasVisitas(navController: AppNavigator, currentUser: User?, authViewModel: AuthViewModel, viewModel: VisitanteViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val minhasVisitas = uiState.visitantes.filter { it.apartmentId == currentUser?.apartmentId }

    MoradorLayout(title = "Minhas Visitas", navigator = navController, currentUser = currentUser, authViewModel = authViewModel) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (minhasVisitas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Nenhuma visita registrada",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(minhasVisitas) { visitante ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = visitante.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "CPF: ${visitante.cpf ?: "N/A"}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { navController.navigate(AppRoute.MinhasVisitasForm(aptId = currentUser?.apartmentId ?: "")) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    }
}

// Meus veículos (Lista Filtrada)
@Composable
fun MeusVeiculos(navController: AppNavigator, currentUser: User?, authViewModel: AuthViewModel, viewModel: VeiculoViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val meusVeiculos = uiState.veiculos.filter { it.apartmentId == currentUser?.apartmentId }

    MoradorLayout(title = "Meus Veículos", navigator = navController, currentUser = currentUser, authViewModel = authViewModel) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (meusVeiculos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                LazyColumn(
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(meusVeiculos) { veiculo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DirectionsCar, null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "${veiculo.brand} ${veiculo.model}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Placa: ${veiculo.plate ?: "N/A"}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { navController.navigate(AppRoute.MeusVeiculosForm(aptId = currentUser?.apartmentId ?: "")) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    }
}

// Perfil do morador
@Composable
fun MeuPerfil(navController: AppNavigator, currentUser: User?, authViewModel: AuthViewModel, onUpdate: (User) -> Unit) {
    MoradorLayout(title = "Meu Perfil", navigator = navController, currentUser = currentUser, authViewModel = authViewModel) { padding ->
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                            text = "Dados Pessoais",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column {
                            Text("NOME", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currentUser?.name ?: "N/A", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 4.dp))
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Column {
                            Text("EMAIL", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currentUser?.email ?: "N/A", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 4.dp))
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Column {
                            Text("CPF", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currentUser?.cpf ?: "N/A", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 4.dp))
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { currentUser?.let { onUpdate(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("SINCRONIZAR PERFIL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// Unidade do morador (Meu Apartamento)
@Composable
fun MeuApartamento(navController: AppNavigator, currentUser: User?, authViewModel: AuthViewModel) {
    val aptRepository = remember { portaria.project.data.repository.ApartmentRepository() }
    var aptNumber by remember { mutableStateOf<String?>(null) }
    var aptBlock by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.apartmentId) {
        val aptId = currentUser?.apartmentId
        if (!aptId.isNullOrBlank()) {
            aptRepository.getById(aptId).onSuccess { apt ->
                aptNumber = apt.number
                aptBlock = apt.block
            }
        }
    }

    MoradorLayout(title = "Meu Apartamento", navigator = navController, currentUser = currentUser, authViewModel = authViewModel) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .width(520.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                            .padding(28.dp)
                    ) {
                        Text(
                            text = "Minha Unidade",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("BLOCO / TORRE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = aptBlock ?: currentUser?.block ?: "N/A",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(50.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text("APTO / NÚMERO", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = aptNumber ?: currentUser?.apartmentId ?: "N/A",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}