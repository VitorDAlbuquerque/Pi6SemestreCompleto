package portaria.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import portaria.project.navigation.AppNavigator
import portaria.project.navigation.AppRoute
import portaria.project.navigation.initialAppBackStack
import portaria.project.data.models.User
import portaria.project.ui.screens.*
import portaria.project.ui.viewmodels.*

// Contextos globais da aplicação (Session State)
val LocalCurrentUser = compositionLocalOf<User?> { null }
val LocalAuthViewModel = compositionLocalOf<AuthViewModel?> { null }

// Guardas de segurança e acesso restrito
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortariaGuard(user: User?, navController: AppNavigator, content: @Composable () -> Unit) {
    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigateReplaceRoot(AppRoute.Login)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    val role = user.role.uppercase().trim()
    val hasAccess = role == "ADMIN" || role == "PORTEIRO" || role == "GERENTE"
    if (hasAccess) {
        content()
    } else {
        AccessDeniedScreen(navController, "Esta área é restrita aos operadores da portaria.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminGuard(user: User?, navController: AppNavigator, content: @Composable () -> Unit) {
    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigateReplaceRoot(AppRoute.Login)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    val role = user.role.uppercase().trim()
    val hasAccess = role == "ADMIN"
    if (hasAccess) {
        content()
    } else {
        AccessDeniedScreen(navController, "Esta área é exclusiva para administradores do sistema.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessDeniedScreen(navController: AppNavigator, message: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acesso Negado") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Área Restrita", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigateReplaceRoot(AppRoute.Home) }) {
                    Text("Voltar ao Painel Principal")
                }
            }
        }
    }
}

// Fluxo principal da aplicação (Theme & ViewModels)
@Composable
fun App() {
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFF6366F1),
        onPrimary = Color.White,
        secondary = Color(0xFF3B82F6),
        onSecondary = Color.White,
        background = Color(0xFF08090C),
        onBackground = Color(0xFFE2E8F0),
        surface = Color(0xFF111319),
        onSurface = Color(0xFFF1F5F9),
        surfaceVariant = Color(0xFF1A1C23),
        onSurfaceVariant = Color(0xFF94A3B8),
        outline = Color(0xFF23252E),
        error = Color(0xFFEF4444),
        onError = Color.White,
        errorContainer = Color(0xFF450A0A),
        onErrorContainer = Color(0xFFFECACA)
    )

    val customShapes = Shapes(
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp)
    )

    MaterialTheme(colorScheme = darkColorScheme, shapes = customShapes) {
        val backStack = remember { initialAppBackStack() }
        val navController = remember(backStack) { AppNavigator(backStack) }
        val authViewModel = remember { AuthViewModel() }
        val apartamentoViewModel = remember { ApartamentoViewModel() }
        val moradorViewModel = remember { MoradorViewModel() }
        val veiculoViewModel = remember { VeiculoViewModel() }
        val visitanteViewModel = remember { VisitanteViewModel() }
        val encomendaViewModel = remember { EncomendaViewModel() }
        val reservaVagaViewModel = remember { ReservaVagaViewModel() }
        val ocorrenciaViewModel = remember { OcorrenciaViewModel() }
        val prestadorViewModel = remember { PrestadorViewModel() }
        val historicoViewModel = remember { HistoricoViewModel() }
        val usuarioViewModel = remember { UsuarioViewModel() }

        val authState by authViewModel.uiState.collectAsState()
        val currentUser = authState.currentUser

        // Mapeamento de rotas e navegação interna (Nav Display)
        CompositionLocalProvider(
            LocalCurrentUser provides currentUser,
            LocalAuthViewModel provides authViewModel
        ) {
            Surface(color = MaterialTheme.colorScheme.background) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { navController.popBackStack() },
                    entryProvider = { key ->
                        NavEntry(key = key, contentKey = key, metadata = emptyMap(), content = { k ->
                            when (k) {
                                is AppRoute.Login -> LoginScreen(navController, authViewModel)
                                is AppRoute.Registro -> RegistroScreen(navController)
                                is AppRoute.Home -> {
                                    when (currentUser?.role?.uppercase()?.trim()) {
                                        "MORADOR" -> MoradorDashboardScreen(navController, currentUser, authViewModel, encomendaViewModel)
                                        "ADMIN", "PORTEIRO", "GERENTE" -> HomeScreen(navController)
                                        else -> {
                                            LaunchedEffect(Unit) {
                                                navController.navigateReplaceRoot(AppRoute.Login)
                                            }
                                        }
                                    }
                                }
                                is AppRoute.ApartamentosLista -> SuperAdminGuard(currentUser, navController) {
                                    LaunchedEffect(Unit) { apartamentoViewModel.loadApartments() }
                                    ApartamentoListScreen(navController, apartamentoViewModel)
                                }
                                is AppRoute.ApartamentosForm -> SuperAdminGuard(currentUser, navController) {
                                    ApartamentoFormScreen(navController, apartamentoViewModel, k.id)
                                }
                                is AppRoute.MoradoresLista -> PortariaGuard(currentUser, navController) {
                                    LaunchedEffect(Unit) { moradorViewModel.loadMoradores() }
                                    MoradorListScreen(navController, moradorViewModel)
                                }
                                is AppRoute.MoradoresForm -> PortariaGuard(currentUser, navController) {
                                    MoradorFormScreen(navController, moradorViewModel, k.id)
                                }
                                is AppRoute.EncomendasLista -> PortariaGuard(currentUser, navController) {
                                    LaunchedEffect(currentUser) { encomendaViewModel.loadEncomendas(currentUser) }
                                    EncomendaListScreen(navController, encomendaViewModel)
                                }
                                is AppRoute.EncomendasForm -> PortariaGuard(currentUser, navController) {
                                    EncomendaFormScreen(navController, encomendaViewModel, k.id)
                                }
                                is AppRoute.ReservasVagasLista -> PortariaGuard(currentUser, navController) {
                                    LaunchedEffect(currentUser) { reservaVagaViewModel.loadReservas(currentUser) }
                                    ReservaVagaListScreen(navController, reservaVagaViewModel)
                                }
                                is AppRoute.ReservasVagasForm -> PortariaGuard(currentUser, navController) {
                                    ReservaVagaFormScreen(navController, reservaVagaViewModel, k.id, k.aptId.ifBlank { null })
                                }
                                is AppRoute.OcorrenciasLista -> PortariaGuard(currentUser, navController) {
                                    LaunchedEffect(currentUser) { ocorrenciaViewModel.loadOcorrencias(currentUser) }
                                    OcorrenciaListScreen(navController, ocorrenciaViewModel)
                                }
                                is AppRoute.OcorrenciasForm -> PortariaGuard(currentUser, navController) {
                                    OcorrenciaFormScreen(navController, ocorrenciaViewModel, k.id)
                                }
                                is AppRoute.PrestadoresLista -> PortariaGuard(currentUser, navController) {
                                    LaunchedEffect(Unit) { prestadorViewModel.loadPrestadores() }
                                    PrestadorListScreen(navController, prestadorViewModel)
                                }
                                is AppRoute.PrestadoresForm -> PortariaGuard(currentUser, navController) {
                                    PrestadorFormScreen(navController, prestadorViewModel, k.id)
                                }
                                is AppRoute.HistoricoAcessosLista -> PortariaGuard(currentUser, navController) {
                                    LaunchedEffect(Unit) { historicoViewModel.loadHistorico() }
                                    HistoricoListScreen(navController, historicoViewModel)
                                }
                                is AppRoute.UsuariosLista -> SuperAdminGuard(currentUser, navController) {
                                    LaunchedEffect(Unit) { usuarioViewModel.loadUsuarios() }
                                    UsuariosListScreen(navController, usuarioViewModel)
                                }
                                is AppRoute.UsuariosForm -> SuperAdminGuard(currentUser, navController) {
                                    UsuariosFormScreen(navController, usuarioViewModel, k.id)
                                }
                                is AppRoute.VisitantesLista -> PortariaGuard(currentUser, navController) {
                                    LaunchedEffect(currentUser) { visitanteViewModel.loadVisitantes(currentUser) }
                                    VisitanteListScreen(navController, visitanteViewModel)
                                }
                                is AppRoute.VisitantesForm -> PortariaGuard(currentUser, navController) {
                                    VisitanteFormScreen(navController, visitanteViewModel, k.id)
                                }
                                is AppRoute.VeiculosLista -> PortariaGuard(currentUser, navController) {
                                    LaunchedEffect(currentUser) { veiculoViewModel.loadVeiculos(currentUser) }
                                    VeiculoListScreen(navController, veiculoViewModel)
                                }
                                is AppRoute.VeiculosForm -> PortariaGuard(currentUser, navController) {
                                    VeiculoFormScreen(navController, veiculoViewModel, k.id)
                                }
                                is AppRoute.MinhasVisitas -> {
                                    LaunchedEffect(currentUser) { visitanteViewModel.loadVisitantes(currentUser) }
                                    MinhasVisitas(navController, currentUser, authViewModel, visitanteViewModel)
                                }
                                is AppRoute.MinhasVisitasForm -> {
                                    VisitanteFormScreen(navController, visitanteViewModel, k.id, k.aptId.ifBlank { null })
                                }
                                is AppRoute.MeuPerfil -> {
                                    MeuPerfil(navController, currentUser, authViewModel) { updated: User ->
                                        authViewModel.updateCurrentUser(updated)
                                    }
                                }
                                is AppRoute.MeuApartamento -> MeuApartamento(navController, currentUser, authViewModel)
                                is AppRoute.MeusVeiculos -> {
                                    LaunchedEffect(currentUser) { veiculoViewModel.loadVeiculos(currentUser) }
                                    MeusVeiculos(navController, currentUser, authViewModel, veiculoViewModel)
                                }
                                is AppRoute.MeusVeiculosForm -> {
                                    VeiculoFormScreen(navController, veiculoViewModel, k.id, k.aptId.ifBlank { null })
                                }
                            }
                        })
                    }
                )
            }
        }
    }
}
