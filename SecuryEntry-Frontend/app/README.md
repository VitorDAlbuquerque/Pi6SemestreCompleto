## SecuryEntry

## Integrantes

Kauan Santos
Samer Halat
Hubert Geremias
Vitor Dias

### Descrição do projeto

O sistema tem como objetivo de auxiliar na portaria de prédios

### Pré-requisitos

- JDK 17+
- Para Android: Android Studio + SDK configurado
- Para iOS: macOS + Xcode (opcional)

### Configuração de API (URL do backend)

O app usa `ApiConfig.BASE_URL` por plataforma:

- Android (emulador): `http://10.0.2.2:9090`
- Desktop / Web / iOS: `http://localhost:9090`

### Como executar

#### Web

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
```

#### Desktop (JVM)

```powershell
.\gradlew.bat :composeApp:run
```

#### Android

Build debug:

```powershell
.\gradlew.bat :composeApp:assembleDebug
```

Ou pelo Android Studio.
