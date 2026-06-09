# SecuryEntry — PI 6° Semestre

Sistema de portaria inteligente com reconhecimento de placas, controle de acesso e app multiplataforma.

## Integrantes

- Kauan Santos
- Samer Halat
- Hubert Geremias
- Vitor Dias

---

## Estrutura do projeto

```
Pi6SemestreCompleto/
├── securyentry-Backend/   # API REST — Spring Boot (Kotlin)
├── SecuryEntry-Frontend/  # App — Compose Multiplatform (Android/Web/Desktop)
├── LPR/                   # Reconhecimento de placas — Python
├── esp32-firmware/        # Firmware do portão — Arduino
├── mosquitto/             # Configuração do broker MQTT local
└── docker-compose.yml     # Sobe o Mosquitto via Docker
```

---

## Pré-requisitos

| Ferramenta | Versão mínima | Para que serve |
|---|---|---|
| Git | qualquer | clonar o projeto |
| JDK | 17+ | Backend + Frontend |
| Python | 3.11+ | LPR |
| Docker | qualquer | Broker MQTT local (opcional) |
| Android Studio | qualquer | Build Android (opcional) |
| Arduino IDE | 2.x | Upload firmware ESP32 (opcional) |

---

## 1. Clonar o repositório

```bash
git clone --recurse-submodules https://github.com/VitorDAlbuquerque/Pi6SemestreCompleto.git
cd Pi6SemestreCompleto
```

> O `--recurse-submodules` é obrigatório — o módulo LPR é um repositório separado.

---

## 2. Backend (Spring Boot)

```bash
cd securyentry-Backend/demo

# Windows
.\gradlew.bat bootRun

# Linux / macOS
./gradlew bootRun
```

A API sobe em `http://localhost:9090`.

**Endpoints disponíveis:**
- `POST /auth/login`
- `POST/GET/PUT/DELETE /users`
- `POST/GET/PUT/DELETE /apartments`
- `POST/GET/PUT/DELETE /visitors`
- `POST/GET/PUT/DELETE /vehicles`
- `POST/GET/PUT/DELETE /encomendas`

---

## 3. Frontend (Compose Multiplatform)

```bash
cd SecuryEntry-Frontend/app
```

### Web

```bash
# Windows
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun

# Linux / macOS
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Abre automaticamente no navegador.

### Desktop (JVM)

```bash
# Windows
.\gradlew.bat :composeApp:run

# Linux / macOS
./gradlew :composeApp:run
```

### Android

```bash
# Gera o APK de debug
.\gradlew.bat :composeApp:assembleDebug
```

O APK fica em `composeApp/build/outputs/apk/debug/`. Ou abra o projeto no Android Studio e rode direto no emulador.

> **Atenção:** no emulador Android use `http://10.0.2.2:9090` como URL do backend (já configurado por padrão).

---

## 4. LPR — Reconhecimento de Placas

```bash
cd LPR

# Criar e ativar o ambiente virtual
python -m venv .venv

# Windows
.venv\Scripts\activate

# Linux / macOS
source .venv/bin/activate

# Instalar dependências
pip install -e .
```

### Modelo YOLO

O modelo de detecção de placas **não está no repositório**. Baixe um modelo YOLOv8 treinado em placas brasileiras e salve como:

```
LPR/models/plate_detector.pt
```

Fontes sugeridas:
- [RodoSol-ALPR](https://github.com/raysonlaroca/rodosol-alpr)
- [Roboflow Universe](https://universe.roboflow.com) — buscar "mercosul license plate"

### Rodar o servidor LPR

```bash
lpr serve
```

### Processar um vídeo

```bash
lpr detect-video /caminho/para/video.mp4
```

### Testar uma imagem

```bash
lpr test-image /caminho/para/placa.jpg
```

> Na primeira execução o EasyOCR baixa os pesos automaticamente (~100 MB). Precisa de internet.

---

## 5. Broker MQTT (Docker — opcional)

O backend já usa o broker público `broker.hivemq.com` por padrão. Se quiser rodar um broker local:

```bash
docker-compose up -d
```

E altere `securyentry-Backend/demo/src/main/resources/application.properties`:

```properties
mqtt.broker-url=tcp://localhost:1883
```

---

## 6. Firmware ESP32

1. Abra o arquivo `esp32-firmware/servo_gate/servo_gate.ino` no Arduino IDE
2. Instale o suporte à placa ESP32: **Boards Manager → ESP32 by Espressif**
3. Selecione a placa correta e a porta COM
4. Clique em **Upload**

---

## Ordem recomendada para rodar tudo junto

```
1. Backend     →  cd securyentry-Backend/demo && .\gradlew.bat bootRun
2. LPR         →  cd LPR && lpr serve
3. Frontend    →  cd SecuryEntry-Frontend/app && .\gradlew.bat :composeApp:run
```
