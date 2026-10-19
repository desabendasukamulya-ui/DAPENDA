package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object PhotoStorageManager {

  private const val PHOTO_DIR_NAME = "resident_photos"
  private const val JPEG_QUALITY = 80
  private const val MAX_DIMENSION = 1200

  fun getPhotoDirectory(context: Context): File {
    val dir = File(context.filesDir, PHOTO_DIR_NAME)
    if (!dir.exists()) {
      dir.mkdirs()
    }
    return dir
  }

  fun saveBitmap(context: Context, bitmap: Bitmap, prefix: String = "photo"): String {
    return try {
      val dir = getPhotoDirectory(context)
      val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
      val file = File(dir, fileName)

      // Scale down if larger than MAX_DIMENSION
      val scaledBitmap = scaleDownIfNeeded(bitmap, MAX_DIMENSION)

      FileOutputStream(file).use { out ->
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        out.flush()
      }
      file.absolutePath
    } catch (e: Throwable) {
      e.printStackTrace()
      ""
    }
  }

  fun saveUri(context: Context, uri: Uri, prefix: String = "photo"): String {
    return try {
      val contentResolver = context.contentResolver

      // 1. Decode bounds to compute appropriate sample size safely without OutOfMemoryError
      var boundsStream: InputStream? = contentResolver.openInputStream(uri) ?: return ""
      val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      BitmapFactory.decodeStream(boundsStream, null, options)
      boundsStream?.close()

      if (options.outWidth <= 0 || options.outHeight <= 0) return ""

      var inSampleSize = 1
      while (options.outWidth / inSampleSize > MAX_DIMENSION * 2 || options.outHeight / inSampleSize > MAX_DIMENSION * 2) {
        inSampleSize *= 2
      }

      // 2. Decode sampled bitmap into RGB_565 (uses 50% less heap RAM)
      val decodeStream = contentResolver.openInputStream(uri) ?: return ""
      val decodeOptions = BitmapFactory.Options().apply {
        this.inSampleSize = inSampleSize
        inPreferredConfig = Bitmap.Config.RGB_565
      }
      val sampledBitmap = BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
      decodeStream.close()
      if (sampledBitmap == null) return ""

      saveBitmap(context, sampledBitmap, prefix)
    } catch (e: Throwable) {
      e.printStackTrace()
      ""
    }
  }

  fun cleanBase64(raw: String): String {
    var s = raw.trim()
    if (s.contains(",")) {
      s = s.substringAfter(",")
    }
    return s.replace("\n", "").replace("\r", "").replace(" ", "").trim()
  }

  fun isBase64String(str: String): Boolean {
    val s = cleanBase64(str)
    if (s.length < 50) return false
    if (s.startsWith("/") || s.startsWith("file://") || s.startsWith("content://")) return false
    return try {
      val testChunk = s.take(64)
      val decoded = Base64.decode(testChunk, Base64.DEFAULT)
      decoded.isNotEmpty()
    } catch (_: Throwable) {
      false
    }
  }

  fun fileToBase64(filePath: String): String {
    return fileToCompressedBase64(filePath)
  }

  /**
   * Mengompres berkas gambar lokal menjadi string Base64 yang ringkas dan cepat disinkronkan ke Cloud Firestore
   * tanpa melampaui batas 1MB per dokumen Firestore.
   */
  fun fileToCompressedBase64(filePath: String, maxDim: Int = 720, quality: Int = 60): String {
    if (filePath.isBlank()) return ""
    return try {
      val clean = cleanBase64(filePath)
      if (isBase64String(clean)) return clean

      val file = File(filePath)
      if (!file.exists() || file.length() == 0L) {
        return ""
      }

      val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
      }
      BitmapFactory.decodeFile(file.absolutePath, options)
      if (options.outWidth <= 0 || options.outHeight <= 0) return ""

      var inSampleSize = 1
      while (options.outWidth / inSampleSize > maxDim * 2 || options.outHeight / inSampleSize > maxDim * 2) {
        inSampleSize *= 2
      }

      val decodeOptions = BitmapFactory.Options().apply {
        this.inSampleSize = inSampleSize
        inPreferredConfig = Bitmap.Config.RGB_565
      }
      val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return ""
      val scaled = scaleDownIfNeeded(bitmap, maxDim)

      val stream = ByteArrayOutputStream()
      scaled.compress(Bitmap.CompressFormat.JPEG, quality, stream)
      val bytes = stream.toByteArray()
      if (scaled != bitmap && !bitmap.isRecycled) {
        bitmap.recycle()
      }
      Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Throwable) {
      e.printStackTrace()
      ""
    }
  }

  /**
   * Memastikan berkas gambar lokal tersedia di perangkat saat ini (misalnya di akun Admin)
   * dengan mendekode data Base64 yang diterima dari Cloud Firestore.
   */
  fun ensureLocalFileFromBase64(context: Context, rawBase64Str: String, prefix: String, idKey: String): String {
    val base64Str = cleanBase64(rawBase64Str)
    if (base64Str.isBlank()) return ""
    return try {
      val cleanKey = idKey.replace(Regex("[^a-zA-Z0-9]"), "_")
      val dir = getPhotoDirectory(context)
      val fileName = "${prefix}_${cleanKey}.jpg"
      val file = File(dir, fileName)
      if (file.exists() && file.length() > 200L) {
        return file.absolutePath
      }
      val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
      if (decodedBytes.isEmpty()) return ""
      FileOutputStream(file).use { out ->
        out.write(decodedBytes)
        out.flush()
      }
      file.absolutePath
    } catch (e: Throwable) {
      e.printStackTrace()
      ""
    }
  }

  fun saveBase64ToFile(context: Context, rawBase64Str: String, prefix: String = "imported"): String {
    val base64Str = cleanBase64(rawBase64Str)
    if (base64Str.isBlank()) return ""
    return try {
      val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
      val dir = getPhotoDirectory(context)
      val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
      val file = File(dir, fileName)
      FileOutputStream(file).use { out ->
        out.write(decodedBytes)
        out.flush()
      }
      file.absolutePath
    } catch (e: Throwable) {
      e.printStackTrace()
      ""
    }
  }

  /**
   * Cek apakah berkas foto ada dan valid (baik berupa File lokal yang ada atau string Base64).
   */
  fun hasPhoto(photoRef: String): Boolean {
    if (photoRef.isBlank()) return false
    val file = File(photoRef)
    if (file.exists() && file.length() > 0) return true
    val clean = cleanBase64(photoRef)
    if (isBase64String(clean)) return true
    return false
  }

  /**
   * Menghasilkan target model gambar yang kompatibel dengan Coil AsyncImage.
   * Mendukung File lokal dan data Bitmap hasil decode Base64.
   */
  fun getPhotoModel(context: Context, photoRef: String): Any? {
    if (photoRef.isBlank()) return null
    val file = File(photoRef)
    if (file.exists() && file.length() > 0) return file

    val clean = cleanBase64(photoRef)
    if (isBase64String(clean)) {
      return try {
        val decodedBytes = Base64.decode(clean, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
      } catch (_: Throwable) {
        null
      }
    }

    // Cek di direktori resident_photos berdasarkan nama file
    val nameOnly = file.name
    val localInDir = File(getPhotoDirectory(context), nameOnly)
    if (localInDir.exists() && localInDir.length() > 0) return localInDir

    return null
  }

  fun deletePhoto(filePath: String) {
    if (filePath.isNotBlank()) {
      try {
        val file = File(filePath)
        if (file.exists()) {
          file.delete()
        }
      } catch (e: Throwable) {
        e.printStackTrace()
      }
    }
  }

  private fun scaleDownIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= maxDim && height <= maxDim) return bitmap

    val ratio: Float = width.toFloat() / height.toFloat()
    val newWidth: Int
    val newHeight: Int
    if (width > height) {
      newWidth = maxDim
      newHeight = (maxDim / ratio).toInt().coerceAtLeast(1)
    } else {
      newHeight = maxDim
      newWidth = (maxDim * ratio).toInt().coerceAtLeast(1)
    }
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
  }
}
