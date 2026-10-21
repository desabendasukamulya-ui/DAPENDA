package com.example.util

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.data.model.Resident
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets

/**
 * Server Web HTTP lokal embedded pada aplikasi SIDAPENDA.
 * Memungkinkan perangkat komputer/laptop di kantor desa atau jaringan Wi-Fi lokal
 * untuk mengakses Web Portal SIDAPENDA secara langsung melalui web browser.
 */
object WebPortalServer {
  private const val TAG = "WebPortalServer"
  const val DEFAULT_PORT = 8080

  private var serverSocket: ServerSocket? = null
  private var serverJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.IO)

  private val _isServerRunning = MutableStateFlow(false)
  val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

  private val _serverAddress = MutableStateFlow("")
  val serverAddress: StateFlow<String> = _serverAddress.asStateFlow()

  // Penyedia data resident untuk REST API lokal
  var residentDataProvider: (() -> List<Resident>)? = null
  var residentAddHandler: ((Resident) -> Unit)? = null

  /**
   * Mengambil alamat IP lokal (Wi-Fi / Hotspot) dari perangkat Android.
   */
  fun getLocalIpAddress(context: Context): String {
    try {
      // 1. Coba dari WifiManager
      val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
      if (wifiManager != null) {
        val ipInt = wifiManager.connectionInfo.ipAddress
        if (ipInt != 0) {
          return String.format(
            "%d.%d.%d.%d",
            ipInt and 0xff,
            ipInt shr 8 and 0xff,
            ipInt shr 16 and 0xff,
            ipInt shr 24 and 0xff
          )
        }
      }

      // 2. Fallback melalui Network Interfaces (termasuk tethering/hotspot)
      val interfaces = NetworkInterface.getNetworkInterfaces()
      while (interfaces.hasMoreElements()) {
        val networkInterface = interfaces.nextElement()
        if (networkInterface.isLoopback || !networkInterface.isUp) continue
        val addresses = networkInterface.inetAddresses
        while (addresses.hasMoreElements()) {
          val address = addresses.nextElement()
          if (!address.isLoopbackAddress && address is Inet4Address) {
            val hostAddress = address.hostAddress
            if (hostAddress != null && hostAddress.isNotBlank()) {
              return hostAddress
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Gagal mendapatkan IP lokal: ${e.message}")
    }
    return "127.0.0.1"
  }

  /**
   * Menjalankan Server Web HTTP Lokal pada port yang ditentukan (default 8080).
   */
  fun startServer(context: Context, port: Int = DEFAULT_PORT) {
    if (_isServerRunning.value) return

    val ip = getLocalIpAddress(context)
    _serverAddress.value = "http://$ip:$port"

    serverJob = scope.launch {
      try {
        serverSocket = ServerSocket(port)
        _isServerRunning.value = true
        Log.i(TAG, "Web Portal Server aktif di ${_serverAddress.value}")

        while (_isServerRunning.value) {
          val clientSocket = try {
            serverSocket?.accept()
          } catch (e: Exception) {
            null
          } ?: break

          scope.launch {
            handleClientRequest(context, clientSocket)
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error pada Server Web: ${e.message}")
        stopServer()
      }
    }
  }

  /**
   * Menghentikan Server Web HTTP Lokal.
   */
  fun stopServer() {
    _isServerRunning.value = false
    try {
      serverSocket?.close()
    } catch (_: Exception) {}
    serverSocket = null
    serverJob?.cancel()
    serverJob = null
    _serverAddress.value = ""
    Log.i(TAG, "Web Portal Server dimatikan.")
  }

  /**
   * Menangani request HTTP dari browser client.
   */
  private fun handleClientRequest(context: Context, client: Socket) {
    try {
      client.use { socket ->
        val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        val output: OutputStream = socket.getOutputStream()
        val writer = PrintWriter(output, true)

        val firstLine = reader.readLine() ?: return
        val parts = firstLine.split(" ")
        if (parts.size < 2) return

        val method = parts[0]
        val path = parts[1]

        // Baca sisa header
        var contentLength = 0
        var line: String?
        while (reader.readLine().also { line = it } != null) {
          if (line!!.isEmpty()) break
          if (line!!.startsWith("Content-Length:", ignoreCase = true)) {
            contentLength = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
          }
        }

        // Routing
        when {
          path == "/" || path.startsWith("/index") -> {
            // Sajikan halaman Web Portal DAPENDA
            val htmlContent = loadWebPortalHtml(context)
            sendResponse(output, "200 OK", "text/html; charset=UTF-8", htmlContent.toByteArray(StandardCharsets.UTF_8))
          }
          path.startsWith("/api/residents") && method == "GET" -> {
            // API Mengambil data seluruh penduduk
            val residents = residentDataProvider?.invoke() ?: emptyList()
            val jsonArray = JSONArray()
            residents.forEach { r ->
              val obj = JSONObject().apply {
                put("id", r.id)
                put("nik", r.nik)
                put("noKk", r.noKk)
                put("namaLengkap", r.namaLengkap)
                put("jenisKelamin", r.jenisKelamin)
                put("tempatLahir", r.tempatLahir)
                put("tanggalLahir", r.tanggalLahir)
                put("usia", r.usia)
                put("rt", r.rt)
                put("rw", r.rw)
                put("dusun", r.dusun)
                put("alamatLengkap", r.alamatLengkap)
                put("agama", r.agama)
                put("statusKeluarga", r.statusKeluarga)
                put("statusKawin", r.statusKawin)
                put("pekerjaan", r.pekerjaan)
                put("pendidikan", r.pendidikan)
                put("golonganDarah", r.golonganDarah)
                put("statusTinggal", r.statusTinggal)
                put("noHp", r.noHp)
                put("bantuanSosial", r.bantuanSosial)
                put("disabilitas", r.disabilitas)
                put("catatan", r.catatan)
              }
              jsonArray.put(obj)
            }
            sendResponse(output, "200 OK", "application/json; charset=UTF-8", jsonArray.toString().toByteArray(StandardCharsets.UTF_8))
          }
          path.startsWith("/api/status") -> {
            val statusJson = JSONObject().apply {
              put("status", "online")
              put("system", "SIDAPENDA Local Web Server")
              put("desa", "Desa Benda, Kecamatan Sukamulya, Kabupaten Tangerang")
              put("totalWarga", residentDataProvider?.invoke()?.size ?: 0)
              put("timestamp", System.currentTimeMillis())
            }
            sendResponse(output, "200 OK", "application/json; charset=UTF-8", statusJson.toString().toByteArray(StandardCharsets.UTF_8))
          }
          else -> {
            val notFound = "<h1>404 Not Found</h1><p>Halaman tidak ditemukan.</p>"
            sendResponse(output, "404 Not Found", "text/html; charset=UTF-8", notFound.toByteArray(StandardCharsets.UTF_8))
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error melayani klien HTTP: ${e.message}")
    }
  }

  private fun sendResponse(output: OutputStream, status: String, contentType: String, body: ByteArray) {
    val headers = StringBuilder()
      .append("HTTP/1.1 $status\r\n")
      .append("Content-Type: $contentType\r\n")
      .append("Content-Length: ${body.size}\r\n")
      .append("Access-Control-Allow-Origin: *\r\n")
      .append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
      .append("Access-Control-Allow-Headers: Content-Type\r\n")
      .append("Connection: close\r\n\r\n")

    output.write(headers.toString().toByteArray(StandardCharsets.UTF_8))
    output.write(body)
    output.flush()
  }

  /**
   * Membaca file HTML Web Portal dari folder assets atau fallback HTML jika belum tersedia.
   */
  fun loadWebPortalHtml(context: Context): String {
    return try {
      context.assets.open("web/index.html").bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    } catch (e: Exception) {
      Log.w(TAG, "Gagal membaca assets web/index.html, menggunakan fallback: ${e.message}")
      generateFallbackHtml()
    }
  }

  private fun generateFallbackHtml(): String {
    return """
      <!DOCTYPE html>
      <html lang="id">
      <head>
        <meta charset="UTF-8">
        <title>SIDAPENDA - Web Portal Kependudukan Desa Benda</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <script src="https://cdn.tailwindcss.com"></script>
      </head>
      <body class="bg-slate-50 font-sans p-6 text-slate-800">
        <div class="max-w-4xl mx-auto bg-white rounded-2xl shadow-md p-8 border border-slate-200">
          <div class="flex items-center space-x-4 mb-6">
            <div class="w-12 h-12 bg-emerald-600 rounded-xl flex items-center justify-center text-white font-bold text-2xl">S</div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900">SIDAPENDA WEB ONLINE</h1>
              <p class="text-sm text-slate-500">Sistem Data Kependudukan Desa Benda • Kabupaten Tangerang</p>
            </div>
          </div>
          <div class="bg-emerald-50 border border-emerald-200 text-emerald-800 p-4 rounded-xl mb-6">
            <p class="font-semibold">Web Portal Server Aktif!</p>
            <p class="text-sm mt-1">Aplikasi Web Online SIDAPENDA terhubung ke database kependudukan Desa Benda.</p>
          </div>
        </div>
      </body>
      </html>
    """.trimIndent()
  }
}
