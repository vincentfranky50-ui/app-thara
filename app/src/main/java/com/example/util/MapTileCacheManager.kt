package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class CacheStats(
    val tileCount: Int,
    val totalSizeBytes: Long
) {
    val formattedSize: String
        get() {
            val mb = totalSizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1.0) "%.1f Mo".format(mb) else "%.0f Ko".format(totalSizeBytes / 1024.0)
        }
}

data class TilePrecacheProgress(
    val isRunning: Boolean = false,
    val currentCount: Int = 0,
    val totalCount: Int = 0,
    val message: String = ""
) {
    val progressFraction: Float
        get() = if (totalCount > 0) (currentCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}

object MapTileCacheManager {
    private const val TAG = "MapTileCacheManager"
    private const val OSM_DIR_NAME = "osmdroid"
    private const val TILES_DIR_NAME = "tiles"
    private const val WEB_CACHE_DIR_NAME = "map_tiles_cache"

    private var isSimulatingOffline: Boolean = false

    fun setSimulateOffline(offline: Boolean) {
        isSimulatingOffline = offline
    }

    fun isSimulateOffline(): Boolean = isSimulatingOffline

    /**
     * Initializes osmdroid configuration with optimized offline storage paths,
     * extended cache lifetimes, and high-capacity limits.
     */
    fun configureOsmdroid(context: Context) {
        try {
            val osmConfig = Configuration.getInstance()
            val prefs = context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
            osmConfig.load(context, prefs)

            val baseDir = File(context.cacheDir, OSM_DIR_NAME)
            if (!baseDir.exists()) baseDir.mkdirs()

            val tileCacheDir = File(baseDir, TILES_DIR_NAME)
            if (!tileCacheDir.exists()) tileCacheDir.mkdirs()

            osmConfig.osmdroidBasePath = baseDir
            osmConfig.osmdroidTileCache = tileCacheDir
            osmConfig.userAgentValue = context.packageName

            // 500 MB maximum cache with 400 MB trim threshold
            osmConfig.tileFileSystemCacheMaxBytes = 500L * 1024 * 1024
            osmConfig.tileFileSystemCacheTrimBytes = 400L * 1024 * 1024

            // 90-day extended offline tile expiration to ensure offline availability
            osmConfig.expirationExtendedDuration = 1000L * 60 * 60 * 24 * 90

            // Multi-threaded tile fetching for smooth zoom & pan
            osmConfig.tileDownloadThreads = 4.toShort()
            osmConfig.tileFileSystemThreads = 4.toShort()

            osmConfig.save(context, prefs)
            Log.d(TAG, "Osmdroid tile cache configured successfully at: ${tileCacheDir.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring osmdroid tile cache", e)
        }
    }

    private fun getWebCacheDir(context: Context): File {
        val dir = File(context.cacheDir, WEB_CACHE_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getHashKey(url: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val bytes = digest.digest(url.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            url.hashCode().toString()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        if (isSimulatingOffline) return false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun handleWebResourceRequest(context: Context, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        return handleUrlRequest(context, url)
    }

    fun handleUrlRequest(context: Context, url: String): WebResourceResponse? {
        val isMapResource = url.contains("arcgisonline.com") ||
                url.contains("google.com/vt") ||
                url.contains("google.com") ||
                url.contains("openstreetmap.org") ||
                url.contains("leaflet") ||
                url.contains("tile") ||
                url.contains("cdnjs.cloudflare.com") ||
                url.contains("cartocdn.com") ||
                url.contains("unpkg.com") ||
                url.endsWith(".png") ||
                url.endsWith(".jpg") ||
                url.endsWith(".jpeg")

        if (!isMapResource) return null

        val cacheDir = getWebCacheDir(context)
        val fileHash = getHashKey(url)
        val extension = when {
            url.contains(".css") -> "css"
            url.contains(".js") -> "js"
            url.contains(".jpg") || url.contains(".jpeg") -> "jpg"
            else -> "png"
        }
        val cachedFile = File(cacheDir, "$fileHash.$extension")

        val mimeType = when (extension) {
            "css" -> "text/css"
            "js" -> "application/javascript"
            "jpg" -> "image/jpeg"
            else -> "image/png"
        }

        // 1. If cached on disk, return cached file stream immediately (FAST & OFFLINE)
        if (cachedFile.exists() && cachedFile.length() > 0) {
            try {
                return WebResourceResponse(
                    mimeType,
                    "UTF-8",
                    FileInputStream(cachedFile)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed reading cached tile file $cachedFile", e)
            }
        }

        // 2. If online, fetch from network and save to disk cache
        if (isNetworkAvailable(context)) {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.instanceFollowRedirects = true
                conn.requestMethod = "GET"
                conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36"
                )

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = conn.inputStream
                    val buffer = ByteArrayOutputStream()
                    val data = ByteArray(4096)
                    var count: Int

                    while (inputStream.read(data, 0, data.size).also { count = it } != -1) {
                        buffer.write(data, 0, count)
                    }
                    val bytes = buffer.toByteArray()

                    // Save to disk cache
                    try {
                        FileOutputStream(cachedFile).use { fos ->
                            fos.write(bytes)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed writing tile cache file $cachedFile", e)
                    }

                    return WebResourceResponse(
                        mimeType,
                        "UTF-8",
                        ByteArrayInputStream(bytes)
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Network fetch failed for $url: ${e.message}")
            }
            return null
        }

        // 3. Fallback when offline and tile not cached: return empty fallback tile image
        if (extension == "png" || extension == "jpg") {
            return generateFallbackTileResponse(mimeType)
        }

        return null
    }

    private fun generateFallbackTileResponse(mimeType: String): WebResourceResponse {
        val bytes = createGridTileByteArray()
        return WebResourceResponse(
            mimeType,
            "UTF-8",
            ByteArrayInputStream(bytes)
        )
    }

    private fun createGridTileByteArray(): ByteArray {
        return byteArrayOf(
            0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(),
            0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0D.toByte(),
            0x49.toByte(), 0x48.toByte(), 0x44.toByte(), 0x52.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(),
            0x08.toByte(), 0x06.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x1F.toByte(), 0x15.toByte(), 0xC4.toByte(), 0x89.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0D.toByte(),
            0x49.toByte(), 0x44.toByte(), 0x41.toByte(), 0x54.toByte(),
            0x78.toByte(), 0x9C.toByte(), 0x63.toByte(), 0x60.toByte(),
            0x60.toByte(), 0x60.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x05.toByte(), 0x00.toByte(), 0x01.toByte(),
            0x0D.toByte(), 0x0A.toByte(), 0x2D.toByte(), 0xB4.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x49.toByte(), 0x45.toByte(), 0x4E.toByte(), 0x44.toByte(),
            0xAE.toByte(), 0x42.toByte(), 0x60.toByte(), 0x82.toByte()
        )
    }

    /**
     * Calculates combined cache statistics across osmdroid storage and web tile cache.
     */
    fun getCombinedCacheStats(context: Context): CacheStats {
        var totalFiles = 0
        var totalBytes = 0L

        fun scanDir(dir: File) {
            if (!dir.exists()) return
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    scanDir(file)
                } else {
                    totalFiles++
                    totalBytes += file.length()
                }
            }
        }

        // Scan Osmdroid directory
        val osmDir = File(context.cacheDir, OSM_DIR_NAME)
        scanDir(osmDir)

        // Scan Web Tile Cache directory
        val webDir = File(context.cacheDir, WEB_CACHE_DIR_NAME)
        scanDir(webDir)

        return CacheStats(totalFiles, totalBytes)
    }

    fun getCacheStats(context: Context): CacheStats = getCombinedCacheStats(context)

    /**
     * Downloads and pre-caches osmdroid tiles for a given bounding box / center point
     * to support offline tracking across zoom levels.
     */
    suspend fun precacheOsmdroidArea(
        context: Context,
        centerLat: Double,
        centerLng: Double,
        radiusKm: Double = 15.0,
        minZoom: Int = 12,
        maxZoom: Int = 16,
        tileSource: ITileSource = TileSourceFactory.MAPNIK,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): Int = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(context)) return@withContext 0

        configureOsmdroid(context)
        val osmTileDir = File(File(context.cacheDir, OSM_DIR_NAME), TILES_DIR_NAME)
        if (!osmTileDir.exists()) osmTileDir.mkdirs()

        // Calculate bounding box in degrees (~111km per latitude degree)
        val latDelta = radiusKm / 111.0
        val lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(centerLat)))

        val north = centerLat + latDelta
        val south = centerLat - latDelta
        val east = centerLng + lngDelta
        val west = centerLng - lngDelta

        var downloadedCount = 0
        var totalTilesCalculated = 0

        // Calculate total tiles for progress tracking
        for (z in minZoom..maxZoom) {
            val n = Math.pow(2.0, z.toDouble())
            val xMin = ((west + 180.0) / 360.0 * n).toInt().coerceAtLeast(0)
            val xMax = ((east + 180.0) / 360.0 * n).toInt().coerceAtMost(n.toInt() - 1)

            val latRadNorth = Math.toRadians(north)
            val latRadSouth = Math.toRadians(south)
            val yMin = ((1.0 - Math.log(Math.tan(latRadNorth) + 1.0 / Math.cos(latRadNorth)) / Math.PI) / 2.0 * n).toInt().coerceAtLeast(0)
            val yMax = ((1.0 - Math.log(Math.tan(latRadSouth) + 1.0 / Math.cos(latRadSouth)) / Math.PI) / 2.0 * n).toInt().coerceAtMost(n.toInt() - 1)

            val countForZoom = (xMax - xMin + 1) * (yMax - yMin + 1)
            totalTilesCalculated += countForZoom
        }

        // Limit download size to reasonable bounds to prevent quota throttling
        val maxAllowedTiles = 350
        var currentTileIndex = 0

        for (z in minZoom..maxZoom) {
            val n = Math.pow(2.0, z.toDouble())
            val xMin = ((west + 180.0) / 360.0 * n).toInt().coerceAtLeast(0)
            val xMax = ((east + 180.0) / 360.0 * n).toInt().coerceAtMost(n.toInt() - 1)

            val latRadNorth = Math.toRadians(north)
            val latRadSouth = Math.toRadians(south)
            val yMin = ((1.0 - Math.log(Math.tan(latRadNorth) + 1.0 / Math.cos(latRadNorth)) / Math.PI) / 2.0 * n).toInt().coerceAtLeast(0)
            val yMax = ((1.0 - Math.log(Math.tan(latRadSouth) + 1.0 / Math.cos(latRadSouth)) / Math.PI) / 2.0 * n).toInt().coerceAtMost(n.toInt() - 1)

            for (x in xMin..xMax) {
                for (y in yMin..yMax) {
                    if (downloadedCount >= maxAllowedTiles) break

                    currentTileIndex++
                    val tileUrl = "https://tile.openstreetmap.org/$z/$x/$y.png"
                    val sourceSubDir = File(osmTileDir, "Mapnik/$z/$x")
                    if (!sourceSubDir.exists()) sourceSubDir.mkdirs()
                    val targetTileFile = File(sourceSubDir, "$y.png.tile")

                    if (!targetTileFile.exists() || targetTileFile.length() == 0L) {
                        try {
                            val conn = URL(tileUrl).openConnection() as HttpURLConnection
                            conn.connectTimeout = 5000
                            conn.readTimeout = 5000
                            conn.setRequestProperty("User-Agent", "TharaFleetTracker/1.0 (Android)")
                            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                                conn.inputStream.use { input ->
                                    FileOutputStream(targetTileFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                downloadedCount++
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Tile download error $tileUrl: ${e.message}")
                        }
                    } else {
                        downloadedCount++
                    }

                    onProgress(downloadedCount, minOf(totalTilesCalculated, maxAllowedTiles))
                }
            }
        }

        return@withContext downloadedCount
    }

    /**
     * Purges all tile cache data from disk.
     */
    fun clearCache(context: Context): Boolean {
        fun deleteRecursively(file: File): Boolean {
            if (file.isDirectory) {
                file.listFiles()?.forEach { deleteRecursively(it) }
            }
            return file.delete()
        }

        val osmDir = File(context.cacheDir, OSM_DIR_NAME)
        val webDir = File(context.cacheDir, WEB_CACHE_DIR_NAME)

        val success1 = if (osmDir.exists()) deleteRecursively(osmDir) else true
        val success2 = if (webDir.exists()) deleteRecursively(webDir) else true

        // Reconfigure osmdroid folders
        configureOsmdroid(context)

        return success1 && success2
    }
}
