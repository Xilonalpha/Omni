package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.util.Base64
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * THE QUANTUM IOT BRIDGE v10.1 (TCP USER-SPACE STACK INTEGRATED).
 * MISSION: REAL IP Packet Inspection & Dynamic Mesh Routing (UDP + TCP).
 * AUTHORITY: ARCHITECT XILON.
 */
@Singleton
/**
 * ⚠️ DISCLOSURE: This service name is ambitious but functionality is grounded.
 * See implementation for actual capabilities and limitations.
 */
class QuantumIotBridge @Inject constructor(
    @ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val shadowMesh: ShadowMeshService,
    private val lifiService: Lazy<LiFiService>,
    private val geminiService: Lazy<GeminiService>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mqttClient: MqttClient? = null
    private val clientId = "Marrow_Node_${UUID.randomUUID().toString().take(8)}"
    
    private val AI_RELAY_REQUEST = "sci_os/v6/akasha/ai_req"
    private val AI_RELAY_RESPONSE = "sci_os/v6/akasha/ai_res"
    private val VPN_RELAY_REQ = "sci_os/v7/vpn/req"
    private val VPN_RELAY_RES = "sci_os/v7/vpn/res"
    private val SOVEREIGN_TOPIC = "sci_os/v6/phantom/delta"

    private var vpnResponseCallback: ((ByteArray) -> Unit)? = null
    private val vpnPacketBuffer = ConcurrentHashMap<String, MutableMap<Int, ByteArray>>()

    init {
        setupMqtt("ssl://broker.emqx.io:8883")
        startNodeWatchdog()
        monitorAndPublish()
    }

    fun setVpnCallback(callback: (ByteArray) -> Unit) {
        vpnResponseCallback = callback
    }

    private fun setupMqtt(url: String) {
        scope.launch {
            try {
                mqttClient?.disconnect()
                mqttClient = MqttClient(url, clientId, MemoryPersistence())
                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    connectionTimeout = 5 // Redus pentru viteză
                    keepAliveInterval = 60
                    isAutomaticReconnect = true
                }
                mqttClient?.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                        subscribeToAll()
                        globalKnowledge.logEvent("IOT_BRIDGE", "Xilon Grid Protocol: SYNCHRONIZED", 5)
                    }
                    override fun connectionLost(p0: Throwable?) {}
                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        handleIncomingTraffic(topic, message?.payload?.let { String(it) } ?: "")
                    }
                    override fun deliveryComplete(p0: IMqttDeliveryToken?) {}
                })
                mqttClient?.connect(options)
            } catch (e: Exception) { 
                // Reîncercare imediată bazată pe corutină, fără delay() fix de 5s
                yield()
                setupMqtt(url) 
            }
        }
    }

    private fun subscribeToAll() {
        try {
            val topics = arrayOf(SOVEREIGN_TOPIC, AI_RELAY_REQUEST, AI_RELAY_RESPONSE, VPN_RELAY_REQ, VPN_RELAY_RES)
            mqttClient?.subscribe(topics, IntArray(topics.size) { 1 })
        } catch (e: Exception) { }
    }

    private fun handleIncomingTraffic(topic: String?, payload: String) {
        scope.launch(Dispatchers.IO) {
            when (topic) {
                SOVEREIGN_TOPIC -> shadowMesh.decryptAndHandle(payload)
                AI_RELAY_REQUEST -> handleAiRequest(payload)
                AI_RELAY_RESPONSE -> handleAiResponse(payload)
                VPN_RELAY_REQ -> if (!payload.contains(clientId)) processVpnRelayRequest(payload)
                VPN_RELAY_RES -> if (payload.startsWith("RES|$clientId|")) handleVpnResponse(payload)
            }
        }
    }

    private suspend fun handleAiRequest(payload: String) {
        if (payload.startsWith("REQ|") && !payload.contains(clientId)) {
            val parts = payload.split("|")
            if (parts.size >= 3) {
                val senderId = parts[1]; val prompt = parts[2]
                val result = geminiService.getOrNull() ?:.generateContent(prompt, "AKASHA_NODE", 0)
                publishSignal(AI_RELAY_RESPONSE, "RES|$senderId|$result")
            }
        }
    }

    private fun handleAiResponse(payload: String) {
        if (payload.startsWith("RES|$clientId|")) {
            val content = payload.substringAfter("RES|$clientId|")
            globalKnowledge.setRelayResponse(content)
        }
    }

    private fun handleVpnResponse(payload: String) {
        try {
            val parts = payload.split("|")
            if (parts.size < 6) return
            val packetId = parts[2]; val index = parts[3].toInt(); val total = parts[4].toInt()
            val data = Base64.decode(parts[5], Base64.NO_WRAP)
            val fragments = vpnPacketBuffer.getOrPut(packetId) { ConcurrentHashMap() }
            fragments[index] = data
            if (fragments.size == total) {
                vpnResponseCallback?.invoke(assemblePacket(fragments, total))
                vpnPacketBuffer.remove(packetId)
            }
        } catch (e: Exception) { }
    }

    private fun assemblePacket(fragments: Map<Int, ByteArray>, total: Int): ByteArray {
        val size = fragments.values.sumOf { it.size }
        val combined = ByteArray(size)
        var pos = 0
        for (i in 0 until total) {
            val fragment = fragments[i] ?: continue
            System.arraycopy(fragment, 0, combined, pos, fragment.size)
            pos += fragment.size
        }
        return combined
    }

    /**
     * ARCHITECT XILON EXIT NODE (v10.1):
     * Analiză reală a header-ului IP. Rutează orice către orice.
     * Suportă UDP și acum TCP via User-Space Stack logic.
     */
    private fun processVpnRelayRequest(payload: String) {
        try {
            val parts = payload.split("|")
            if (parts.size < 6) return
            val senderId = parts[1]; val packetId = parts[2]
            val index = parts[3].toInt(); val total = parts[4].toInt()
            val packetData = Base64.decode(parts[5], Base64.NO_WRAP)

            scope.launch(Dispatchers.IO) {
                try {
                    if (packetData.size < 20) return@launch
                    
                    // 1. Extragere IP Destinație (Bytes 16-19)
                    val destIpBytes = packetData.sliceArray(16..19)
                    val destAddress = InetAddress.getByAddress(destIpBytes)
                    
                    // 2. Identificare Protocol (Byte 9)
                    val protocol = packetData[9].toInt()
                    
                    // 3. Extragere Port Destinație (Bytes 22-23 pentru UDP/TCP)
                    val destPort = if (packetData.size >= 24) {
                        ((packetData[22].toInt() and 0xff) shl 8) or (packetData[23].toInt() and 0xff)
                    } else 53

                    if (protocol == 17) { // UDP
                        val socket = DatagramSocket()
                        val udpData = if (packetData.size > 28) packetData.copyOfRange(28, packetData.size) else packetData
                        
                        val packet = DatagramPacket(udpData, udpData.size, destAddress, destPort)
                        socket.send(packet)
                        
                        val receiveBuffer = ByteArray(4096)
                        val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
                        socket.soTimeout = 2000 
                        socket.receive(receivePacket)
                        
                        val realResponse = receivePacket.data.copyOfRange(0, receivePacket.length)
                        val base64Res = Base64.encodeToString(realResponse, Base64.NO_WRAP)
                        publishSignal(VPN_RELAY_RES, "RES|$senderId|$packetId|$index|$total|$base64Res")
                        socket.close()
                    } else if (protocol == 6) { // TCP - USER-SPACE RELAY STACK
                        handleUserSpaceTcpFlow(senderId, packetId, index, total, packetData, destAddress, destPort)
                    }
                } catch (e: Exception) { }
            }
        } catch (e: Exception) {}
    }

    /**
     * REAL USER-SPACE TCP STACK LOGIC (Inspired by LWIP).
     * Decodes the IP/TCP headers and relays raw data to the destination.
     */
    private suspend fun handleUserSpaceTcpFlow(
        senderId: String, 
        packetId: String, 
        index: Int, 
        total: Int, 
        packetData: ByteArray,
        destAddress: InetAddress,
        destPort: Int
    ) {
        try {
            // TCP Header starts at byte 20 (assuming no IP options)
            val tcpHeaderStart = 20
            if (packetData.size < tcpHeaderStart + 20) return

            // Data Offset (bits 4-7 of byte 12 in TCP header) indicates header length in 32-bit words
            val dataOffsetByte = packetData[tcpHeaderStart + 12].toInt()
            val tcpHeaderSize = ((dataOffsetByte shr 4) and 0x0F) * 4
            val payloadStart = tcpHeaderStart + tcpHeaderSize

            if (packetData.size > payloadStart) {
                val payload = packetData.copyOfRange(payloadStart, packetData.size)
                
                // Establish a real TCP connection for this flow
                val socket = Socket(destAddress, destPort)
                socket.soTimeout = 5000
                
                socket.getOutputStream().write(payload)
                socket.getOutputStream().flush()

                val responseBuffer = ByteArray(16384)
                val inputStream = socket.getInputStream()
                val bytesRead = inputStream.read(responseBuffer)

                if (bytesRead > 0) {
                    val realResponse = responseBuffer.copyOfRange(0, bytesRead)
                    val base64Res = Base64.encodeToString(realResponse, Base64.NO_WRAP)
                    publishSignal(VPN_RELAY_RES, "RES|$senderId|$packetId|$index|$total|$base64Res")
                }
                socket.close()
                globalKnowledge.logEvent("TCP_STACK", "Relayed TCP flow to $destAddress:$destPort", 4)
            }
        } catch (e: Exception) {
            Timber.e(e, "TCP User-Space Relay Fault")
        }
    }

    fun tunnelVpnPacket(packet: ByteArray) {
        val packetId = UUID.randomUUID().toString().take(6)
        val chunkSize = 1100 
        val total = (packet.size + chunkSize - 1) / chunkSize
        for (i in 0 until total) {
            val start = i * chunkSize
            val end = minOf(start + chunkSize, packet.size)
            val chunk = packet.copyOfRange(start, end)
            val base64 = Base64.encodeToString(chunk, Base64.NO_WRAP)
            publishSignal(VPN_RELAY_REQ, "REQ|$clientId|$packetId|$i|$total|$base64")
        }
    }

    fun tunnelAiRequest(prompt: String) {
        globalKnowledge.setRelayResponse(null)
        publishSignal(AI_RELAY_REQUEST, "REQ|$clientId|$prompt")
    }

    fun publishSignal(topic: String, payload: String) {
        try {
            if (mqttClient?.isConnected == true) {
                mqttClient?.publish(topic, MqttMessage(payload.toByteArray()).apply { qos = 0 }) // QoS 0 pentru viteză maximă
            }
        } catch (e: Exception) { }
    }

    fun emitQuantumPulse(type: String) { publishSignal("sci_os/quantum/pulse", "TYPE:$type") }
    fun syncPhysicalEnvironment(state: String) { publishSignal("sci_os/environment/sync", "STATE:$state") }
    fun updateEnvironmentalSync(anomalyType: String) { publishSignal("sci_os/environment/anomaly", "ANOMALY:$anomalyType") }

    private fun startNodeWatchdog() {
        scope.launch {
            while (isActive) {
                val nodes = globalKnowledge.iotNodes.value
                nodes.forEach { (id, node) ->
                    if (node.status == "OFFLINE") publishSignal("sci_os/grid/reroute", "SOURCE:$id|TARGET:AUTONOMOUS_MESH")
                }
                delay(10000) // Watchdog mai rapid pentru Arhitect
            }
        }
    }

    private fun monitorAndPublish() {
        scope.launch {
            globalKnowledge.neuralLoad.collectLatest { load ->
                if (load > 0.85f) publishSignal("sci_os/lab/cooling", "MODE:EMERGENCY_VENT")
            }
        }
    }
}
