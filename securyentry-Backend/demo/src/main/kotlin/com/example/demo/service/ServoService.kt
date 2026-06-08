package com.example.demo.service

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ServoService(
    @Value("\${mqtt.broker-url:tcp://localhost:1883}") private val brokerUrl: String,
    @Value("\${mqtt.topic.portao:securyentry/portao/cmd}") private val topic: String,
) {
    fun abrirPortao() {
        val client = MqttClient(brokerUrl, MqttClient.generateClientId())
        try {
            client.connect()
            val message = MqttMessage("OPEN".toByteArray())
            message.qos = 1
            client.publish(topic, message)
        } finally {
            if (client.isConnected) client.disconnect()
            client.close()
        }
    }
}
