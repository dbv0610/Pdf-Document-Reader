package com.wxiwei.office.system

import java.io.IOException
import java.io.InputStream
import java.net.Socket
import java.net.UnknownHostException

class SocketClient {
    private var client: Socket? = null

    init {
        initConnection()
    }

    fun initConnection() {
        try {
            client = Socket(HOST, LISTENER_PORT)
        } catch (e: UnknownHostException) {
            println("Error setting up socket connection: unknown host at $HOST:$LISTENER_PORT")
        } catch (e: IOException) {
            println("Error setting up socket connection: $e")
        }
    }

    fun getFile(fileName: String): InputStream? {
        return try {
            val output = client!!.getOutputStream()
            output.write(fileName.toByteArray())
            output.flush()
            client!!.getInputStream()
        } catch (e: Exception) {
            println("Error reading from file: $fileName")
            null
        }
    }

    companion object {
        const val HOST = "172.25.3.147"
        const val LISTENER_PORT = 3000
        private val sc = SocketClient()

        @JvmStatic
        fun instance(): SocketClient = sc
    }
}
