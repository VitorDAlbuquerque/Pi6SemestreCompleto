package portaria.project.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun provideHttpClient(): HttpClient = HttpClient(Js) {
    defaultConfig()
}
