package ram.talia.hexal.api.everbook

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import java.io.*
import java.io.File
import java.io.FileWriter
import java.nio.ByteBuffer
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.util.*
import java.util.zip.ZipException
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


// https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-security/src/main/java/com/baeldung/encrypt/FileEncrypterDecrypter.java

internal class FileEncrypterDecrypter(private val secretKey: SecretKey, cipher: String) {
	private val cipher: Cipher

	init {
		this.cipher = Cipher.getInstance(cipher)
	}

	@Throws(InvalidKeyException::class, IOException::class)
	fun encrypt(content: String, file: File) {
		val fileWriter = FileWriter(file)
		fileWriter.write(content)
		fileWriter.close()
	}

	@Throws(InvalidKeyException::class, IOException::class)
	fun encrypt(content: CompoundTag, file: File) {
		file.absoluteFile.parentFile.mkdirs()
		file.createNewFile()
		FileOutputStream(file).use { fileOut ->

			NbtIo.writeCompressed(content,fileOut)
		}
	}

	@Throws(InvalidAlgorithmParameterException::class, InvalidKeyException::class, IOException::class)
	fun decrypt(file: File): String {
		val stringBuilder = StringBuilder()

		val bufferedReader = BufferedReader(file.reader())

		bufferedReader.useLines { lines ->
			lines.forEach { line ->
				stringBuilder.append(line).append("\n")
			}
		}

    	return stringBuilder.toString()
	}

	@Throws(InvalidAlgorithmParameterException::class, InvalidKeyException::class, IOException::class)
	fun decryptCompound(file: File): CompoundTag? {
		var content: CompoundTag?

		if (!file.exists())
			return null

		FileInputStream(file).use { fileIn ->
			try {
				content = NbtIo.readCompressed(fileIn)
			} catch (e: ZipException) {
				content = null
			}
		}

		return content
	}

	companion object {
		fun getKey(uuid: UUID, cipher: String): SecretKey {
			return SecretKeySpec(longsToBytes(uuid.mostSignificantBits, uuid.leastSignificantBits), cipher)
		}

		fun longsToBytes(x: Long, y: Long): ByteArray {
			val buffer: ByteBuffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES)
			buffer.putLong(x)
			buffer.putLong(y)
			return buffer.array()
		}
	}
}