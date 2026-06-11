package portaria.project.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import portaria.project.LocalCurrentUser
import portaria.project.data.models.User
import portaria.project.navigation.AppNavigator
import portaria.project.navigation.AppRoute

// Estrutura do template (SaaSLayout Composable)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaaSLayout(
    title: String,
    navigator: AppNavigator,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentUser = LocalCurrentUser.current
    val role = currentUser?.role?.uppercase()?.trim()
    val isAdmin = role == "ADMIN"
    val isPortaria = role == "ADMIN" || role == "PORTEIRO" || role == "GERENTE"

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 960.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                SidebarContent(
                    currentUser = currentUser,
                    isAdmin = isAdmin,
                    isPortaria = isPortaria,
                    activeTitle = title,
                    navigator = navigator
                )

                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outline))

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            actions = {
                                HeaderActions(navigator)
                            },
                            modifier = Modifier.shadow(0.dp)
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    content = content
                )
            }
        } else {
            // Layout Responsivo Mobile (Drawer Deslizante)
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                    ) {
                        SidebarContent(
                            currentUser = currentUser,
                            isAdmin = isAdmin,
                            isPortaria = isPortaria,
                            activeTitle = title,
                            navigator = navigator,
                            isFloatingDrawer = true,
                            onCloseDrawer = { scope.launch { drawerState.close() } }
                        )
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                                actionIconContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            actions = {
                                HeaderActions(navigator)
                            }
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    content = content
                )
            }
        }
    }
}

// Conteúdo e branding da sidebar esquerda
@Composable
private fun SidebarContent(
    currentUser: User?,
    isAdmin: Boolean,
    isPortaria: Boolean,
    activeTitle: String,
    navigator: AppNavigator,
    isFloatingDrawer: Boolean = false,
    onCloseDrawer: () -> Unit = {}
) {
    val modifier = if (isFloatingDrawer) {
        Modifier.fillMaxHeight().width(320.dp)
    } else {
        Modifier.fillMaxHeight().width(280.dp)
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 24.dp)
    ) {
        // Logotipo & Branding
        Row(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "SecureEntry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "CONTROLE DE ACESSO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(Modifier.height(16.dp))

        // Itens de Menu
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SidebarItem(
                icon = Icons.Default.Home,
                label = "Dashboard Operacional",
                isActive = activeTitle == "Dashboard Operacional"
            ) {
                onCloseDrawer()
                navigator.navigate(AppRoute.Home)
            }

            if (isPortaria) {
                SidebarItem(
                    icon = Icons.Default.Person,
                    label = "Moradores",
                    isActive = activeTitle == "Moradores"
                ) {
                    onCloseDrawer()
                    navigator.navigate(AppRoute.MoradoresLista)
                }
            }

            if (isAdmin) {
                SidebarItem(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Unidades",
                    isActive = activeTitle == "Apartamentos" || activeTitle == "Unidades"
                ) {
                    onCloseDrawer()
                    navigator.navigate(AppRoute.ApartamentosLista)
                }
                SidebarItem(
                    icon = Icons.Default.Settings,
                    label = "Acessos Administrativos",
                    isActive = activeTitle == "Acessos Administrativos" || activeTitle == "Usuários"
                ) {
                    onCloseDrawer()
                    navigator.navigate(AppRoute.UsuariosLista)
                }
            }

            if (isPortaria) {
                SidebarItem(
                    icon = Icons.Default.Face,
                    label = "Visitantes",
                    isActive = activeTitle == "Visitantes"
                ) {
                    onCloseDrawer()
                    navigator.navigate(AppRoute.VisitantesLista)
                }
                SidebarItem(
                    icon = Icons.Default.DirectionsCar,
                    label = "Veículos",
                    isActive = activeTitle == "Veículos"
                ) {
                    onCloseDrawer()
                    navigator.navigate(AppRoute.VeiculosLista)
                }
                SidebarItem(
                    icon = Icons.Default.Build,
                    label = "Prestadores de Serviço",
                    isActive = activeTitle == "Prestadores de Serviço"
                ) {
                    onCloseDrawer()
                    navigator.navigate(AppRoute.PrestadoresLista)
                }
                SidebarItem(
                    icon = Icons.Default.Email,
                    label = "Encomendas",
                    isActive = activeTitle == "Encomendas"
                ) {
                    onCloseDrawer()
                    navigator.navigate(AppRoute.EncomendasLista)
                }
                SidebarItem(
                    icon = Icons.Default.LocalParking,
                    label = "Reservas de Vagas",
                    isActive = activeTitle == "Reservas de Vagas"
                ) {
                    onCloseDrawer()
                    navigator.navigate(AppRoute.ReservasVagasLista)
                }
                SidebarItem(
                    icon = Icons.Default.ReportProblem,
                    label = "Ocorrencias",
                    isActive = activeTitle == "Ocorrencias"
                ) {
                    onCloseDrawer()
                    navigator.navigate(AppRoute.OcorrenciasLista)
                }
                SidebarItem(
                    icon = Icons.Default.History,
                    label = "Histórico de Acessos",
                    isActive = activeTitle == "Histórico de Acessos"
                ) {
                    onCloseDrawer()
                    navigator.navigate(AppRoute.HistoricoAcessosLista)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(Modifier.height(16.dp))

        // Rodapé com Perfil do Operador
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser?.name?.take(2)?.uppercase() ?: "OP",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentUser?.name ?: "Desconhecido",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentUser?.role ?: "OPERADOR",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            val authViewModel = portaria.project.LocalAuthViewModel.current
            IconButton(
                onClick = {
                    onCloseDrawer()
                    if (authViewModel != null) {
                        authViewModel.logout {
                            navigator.navigateReplaceRoot(AppRoute.Login)
                        }
                    } else {
                        navigator.navigateReplaceRoot(AppRoute.Login)
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = "Sair",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Item selecionável de navegação
@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
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
            // Barra indicadora deslizante no active
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

// Ações da barra superior (Header Actions)
@Composable
private fun HeaderActions(navigator: AppNavigator) {
    IconButton(
        onClick = { },
        modifier = Modifier
            .padding(end = 8.dp)
            .shadow(0.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
            .size(36.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notificações",
            modifier = Modifier.size(18.dp)
        )
    }

    IconButton(
        onClick = { navigator.navigate(AppRoute.MeuPerfil) },
        modifier = Modifier
            .shadow(0.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
            .size(36.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Perfil",
            modifier = Modifier.size(20.dp)
        )
    }
}
