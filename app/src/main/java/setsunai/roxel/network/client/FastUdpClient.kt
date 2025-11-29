package setsunai.roxel.network.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import setsunai.roxel.network.client.base.ClientBase
import setsunai.roxel.network.client.data.ConnectionState
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class FastUdpClient(private val stateFlow: MutableStateFlow<ConnectionState>) : ClientBase() {
    private var socket: DatagramSocket? = null
    private var address: InetAddress? = null
    private var port: Int = -1

    private val clientMutex = Mutex()
    private var credentialsHash: Long = -1

    override suspend fun onLaunch(): Job = withContext(Dispatchers.IO) {
        supervisorScope {
            launch(Dispatchers.IO) {
                combine(
                    credentialsFlow.filterNotNull(),
                    stateFlow
                ) { credentials, state -> credentials to state }
                    .collectLatest { (credentials, state) ->
                        if (credentialsHash != credentials.id) {
                            stateFlow.emit(ConnectionState.UNAVAILABLE)
                            credentialsHash = credentials.id
                        }
                        when (state) {
                            ConnectionState.SUCCESS -> {
                                connect(credentials.ip, credentials.fastUdpPort)
                            }
                            else -> disconnect()
                        }
                    }
            }
        }
    }

    suspend fun send(message: String) {
        clientMutex.withLock {
            try {
                val data = "${message}\r\n\r\n".toByteArray()
                val sendPacket =
                    DatagramPacket(data, data.size, address, port)
                socket?.send(sendPacket)
            } catch (_: Throwable) {
            }
        }
    }

    private fun connect(ip: String, port: Int) {
        disconnect()
        try {
            socket = DatagramSocket()
            address = InetAddress.getByName(ip)
            this.port = port
        } catch (_: Throwable) {
            disconnect()
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (_: Exception) {
        }

        address = null
        socket = null
        port = -1
    }

    private fun closeClient() {
        disconnect()
    }

    override fun onClose() {
        closeClient()
    }
}