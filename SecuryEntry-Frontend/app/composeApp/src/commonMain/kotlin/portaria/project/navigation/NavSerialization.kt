package portaria.project.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal val appNavSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AppRoute.Login::class, AppRoute.Login.serializer())
            subclass(AppRoute.Registro::class, AppRoute.Registro.serializer())
            subclass(AppRoute.Home::class, AppRoute.Home.serializer())
            subclass(AppRoute.ApartamentosLista::class, AppRoute.ApartamentosLista.serializer())
            subclass(AppRoute.ApartamentosForm::class, AppRoute.ApartamentosForm.serializer())
            subclass(AppRoute.MoradoresLista::class, AppRoute.MoradoresLista.serializer())
            subclass(AppRoute.MoradoresForm::class, AppRoute.MoradoresForm.serializer())
            subclass(AppRoute.VisitantesLista::class, AppRoute.VisitantesLista.serializer())
            subclass(AppRoute.VisitantesForm::class, AppRoute.VisitantesForm.serializer())
            subclass(AppRoute.MinhasVisitas::class, AppRoute.MinhasVisitas.serializer())
            subclass(AppRoute.MinhasVisitasForm::class, AppRoute.MinhasVisitasForm.serializer())
            subclass(AppRoute.VeiculosLista::class, AppRoute.VeiculosLista.serializer())
            subclass(AppRoute.VeiculosForm::class, AppRoute.VeiculosForm.serializer())
            subclass(AppRoute.EncomendasLista::class, AppRoute.EncomendasLista.serializer())
            subclass(AppRoute.EncomendasForm::class, AppRoute.EncomendasForm.serializer())
            subclass(AppRoute.MeuPerfil::class, AppRoute.MeuPerfil.serializer())
            subclass(AppRoute.MeuApartamento::class, AppRoute.MeuApartamento.serializer())
            subclass(AppRoute.MeusVeiculos::class, AppRoute.MeusVeiculos.serializer())
            subclass(AppRoute.MeusVeiculosForm::class, AppRoute.MeusVeiculosForm.serializer())
        }
    }
}
