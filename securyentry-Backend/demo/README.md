## SecuryEntry

## Integrantes

Kauan Santos
Samer Halat
Hubert Geremias
Vitor Dias

### Descrição do projeto

O sistema tem como objetivo de auxiliar na portaria de prédios

### Endpoints

- Autenticação: `POST /auth/login`
- Usuários: `POST/GET/PUT/DELETE /users`
- Apartamentos: `POST/GET/PUT/DELETE /apartments`
- Visitantes: `POST/GET/PUT/DELETE /visitors`
- Veículos: `POST/GET/PUT/DELETE /vehicles`

Por padrão o servidor sobe em `http://localhost:9090`.

### Pré-requisitos

- JDK 17

### Como executar

```powershell
.\gradlew.bat bootRun
```

#### Testes

```powershell
.\gradlew.bat test
```

### Integração com o frontend

- Desktop / Web / iOS: `http://localhost:9090`
- Android emulador: `http://10.0.2.2:9090`

Para mudar a porta, edite os arquivos:

- `demo/src/main/resources/application.properties`
- `app/composeApp/src/*Main/.../ApiConfig.*.kt`
