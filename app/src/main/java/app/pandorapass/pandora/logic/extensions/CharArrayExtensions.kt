package app.pandorapass.pandora.logic.extensions

/**
 * A helper function that converts a char array to a byte array.
 *
 * @return The char array represented as a byte array.
 */
fun CharArray.toByteArray(): ByteArray {
    val charBuffer = java.nio.CharBuffer.wrap(this)
    val byteBuffer = java.nio.charset.StandardCharsets.UTF_8.encode(charBuffer)

    val bytes = ByteArray(byteBuffer.remaining())
    byteBuffer.get(bytes)

    if (byteBuffer.hasArray()) {
        byteBuffer.array().fill(0)
    }

    return bytes
}