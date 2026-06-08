package portaria.project.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun provideHttpClient(): HttpClient = HttpClient(Darwin) {
    defaultConfig()
}
