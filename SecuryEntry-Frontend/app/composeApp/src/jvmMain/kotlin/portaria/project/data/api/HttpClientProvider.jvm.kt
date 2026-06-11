package portaria.project.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

private val sharedClient: HttpClient by lazy {
    HttpClient(OkHttp) {
        defaultConfig()
    }
}

actual fun provideHttpClient(): HttpClient = sharedClient
