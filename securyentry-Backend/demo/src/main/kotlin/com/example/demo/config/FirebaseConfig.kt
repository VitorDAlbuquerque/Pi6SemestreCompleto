package com.example.demo.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FirebaseConfig {

    @Bean
    fun firebaseApp(): FirebaseApp {
        val credentialsStream = credentialsFromEnv()
            ?: credentialsFromFile()
            ?: error(
                "Firebase credentials not found. " +
                "Set FIREBASE_CREDENTIALS_BASE64 env var or place the service account JSON in resources."
            )

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(credentialsStream))
            .build()

        return if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        } else {
            FirebaseApp.getInstance()
        }
    }

    // Produção / CI-CD: credencial em Base64 na variável de ambiente FIREBASE_CREDENTIALS_BASE64
    private fun credentialsFromEnv(): java.io.InputStream? {
        val b64 = System.getenv("FIREBASE_CREDENTIALS_BASE64") ?: return null
        val bytes = java.util.Base64.getDecoder().decode(b64)
        return bytes.inputStream()
    }

    // Desenvolvimento local: arquivo JSON no classpath (gitignored)
    private fun credentialsFromFile(): java.io.InputStream? =
        this::class.java.getResourceAsStream("/pi66-1514a-firebase-adminsdk-fbsvc-43a2ccfcc7.json")
}