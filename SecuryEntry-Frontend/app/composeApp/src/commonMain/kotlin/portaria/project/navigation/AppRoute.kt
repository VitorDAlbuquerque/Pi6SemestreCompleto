package portaria.project.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute : NavKey {

    // --- AUTENTICAÇÃO ---
    @Serializable data object Login : AppRoute()
    @Serializable data object Registro : AppRoute()
    @Serializable data object Home : AppRoute()

    // --- MÓDULOS ADMINISTRATIVOS ---
    @Serializable data object ApartamentosLista : AppRoute()
    @Serializable data class ApartamentosForm(val id: String = "") : AppRoute()

    @Serializable data object MoradoresLista : AppRoute()
    @Serializable data class MoradoresForm(val id: String = "") : AppRoute()

    @Serializable data object VisitantesLista : AppRoute()
    @Serializable data class VisitantesForm(val id: String = "") : AppRoute()

    @Serializable data object VeiculosLista : AppRoute()
    @Serializable data class VeiculosForm(val id: String = "") : AppRoute()

    @Serializable data object EncomendasLista : AppRoute()
    @Serializable data class EncomendasForm(val id: String = "") : AppRoute()

    @Serializable data object PrestadoresLista : AppRoute()
    @Serializable data class PrestadoresForm(val id: String = "") : AppRoute()

    @Serializable data object UsuariosLista : AppRoute()
    @Serializable data class UsuariosForm(val id: String = "") : AppRoute()

    @Serializable data object HistoricoAcessosLista : AppRoute()

    // --- MÓDULOS DO MORADOR (Acesso restrito) ---
    @Serializable data object MeuPerfil : AppRoute()
    @Serializable data object MeuApartamento : AppRoute()

    @Serializable data object MeusVeiculos : AppRoute()
    @Serializable data class MeusVeiculosForm(val aptId: String = "", val id: String = "") : AppRoute()

    @Serializable data class MinhasVisitas(val aptId: String = "") : AppRoute()
    @Serializable data class MinhasVisitasForm(val aptId: String = "", val id: String = "") : AppRoute()
}