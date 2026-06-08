#include <WiFi.h>
#include <PubSubClient.h>
#include <ESP32Servo.h>

// --- Configuração (edite antes de gravar) ---
#define WIFI_SSID     "DIAS ALBU"
#define WIFI_PASSWORD "12344321"
#define MQTT_BROKER   "broker.hivemq.com"
#define MQTT_PORT     1883
#define MQTT_TOPIC    "securyentry/xk92mf7t/portao/cmd"
// --------------------------------------------

const int SERVO_PIN      = 18;
const int ANGLE_CLOSED   = 0;
const int ANGLE_OPEN     = 90;
const unsigned long OPEN_MS = 3000;

WiFiClient   espClient;
PubSubClient mqtt(espClient);
Servo        servo;

void onMessage(char* topic, byte* payload, unsigned int length) {
  String msg;
  for (unsigned int i = 0; i < length; i++) msg += (char)payload[i];

  if (msg == "OPEN") {
    servo.write(ANGLE_OPEN);
    delay(OPEN_MS);
    servo.write(ANGLE_CLOSED);
  }
}

void connectWifi() {
  Serial.print("Conectando WiFi");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println(" OK — IP: " + WiFi.localIP().toString());
}

void reconnectMqtt() {
  while (!mqtt.connected()) {
    Serial.print("Conectando MQTT...");
    if (mqtt.connect("esp32-portao")) {
      Serial.println("OK");
      mqtt.subscribe(MQTT_TOPIC);
    } else {
      Serial.println("falhou, tentando em 5s");
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  servo.attach(SERVO_PIN);
  servo.write(ANGLE_CLOSED);
  connectWifi();
  mqtt.setServer(MQTT_BROKER, MQTT_PORT);
  mqtt.setCallback(onMessage);
}

void loop() {
  if (!mqtt.connected()) reconnectMqtt();
  mqtt.loop();
}
