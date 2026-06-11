package portaria.project.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

private val sharedClient: HttpClient by lazy {
    HttpClient(Android) {
        defaultConfig()
    }
}

actual fun provideHttpClient(): HttpClient = sharedClient
