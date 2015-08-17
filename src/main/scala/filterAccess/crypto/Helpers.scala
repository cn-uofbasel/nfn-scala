package filterAccess.crypto

import java.security.MessageDigest
import java.util.Base64 // note: this is new in java 8
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec

import java.security.KeyPairGenerator

/**
 * Created by Claudio Marxer <marxer@claudio.li>
 *
 * Helpers for encryption/decryption, encoding/decoding, hashing, key generation.
 *
 * NOTE: This file makes use of Java 8 features.
 *
 */
object Helpers {


  /**
   * Convert a Array[Byte] to a String (base64 encoding)
   * Inverse function of stringToByte(...)
   *
   * NOTE: Output String has more characters than length of data:Byte[Array] because of base64 encoding.
   *
   * @param   data  Data to convert
   * @return        Same data as String (base64 encoding)
   */
  def byteToString(data: Array[Byte]): String =
    Base64.getEncoder.encodeToString(data) // note: this is new in java 8


  /**
   * Convert a String (base64 encoding) to a Array[Byte].
   * Inverse function of byteToString(...)
   *
   * NOTE: data:String has more characters than length of returned Byte[Array] because of base64 encoding.
   *
   * @param   data  Data to convert (base64 encoding)
   * @return        Same data as Array[Byte]
   */
  def stringToByte(data: String): Array[Byte] =
    Base64.getDecoder.decode(data) // note: this is new in java 8


  /**
   * Compute a SHA-256 hash of a given Array[Byte].
   * The length of the returned hash is 256 bit or 32 byte respectively.
   *
   * @param   s  Data to calculate a hash
   * @return     SHA-256 Hash (length: 256 bit or 32 byte)
   */
  def computeHash(s: Array[Byte]): Array[Byte] = {

    // initialize digester
    val digester = MessageDigest.getInstance("SHA-256")
    digester.update(s)

    // compute SHA-256 hash
    digester.digest

  }


  /**
   * Compute a SHA-256 hash of a given String.
   * The length of the returned hash is 256 bit or 32 byte respectively.
   *
   * @param   s  Data to calculate a hash
   * @return     SHA-256 Hash (length: 256 bit or 32 byte, base64 encoded)
   */
  def computeHash(s:String):String = byteToString(computeHash(s.getBytes))


  /**
   * Do the actual work for symmetric encryption with AES-256 (AES/CBC/PKCS5Padding).
   *
   * @param   data    Data to encrypt
   * @param   key     Encryption key
   * @return          Result of encryption
   */
  def symEncryptProcessing(data: Array[Byte], key: Array[Byte]): Array[Byte] = {

    // initialize encryption key
    val encryptionKey = new SecretKeySpec(key, "AES")

    // initialize cypher
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val initVec = computeHash("asfasfsafsaf".getBytes).drop(16)
    val initVecSpec = new IvParameterSpec(initVec)
    cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, initVecSpec)

    // do encryption
    cipher.doFinal(data)

  }


  /**
   * Do the actual work for symmetric decryption with AES-256 (AES/CBC/PKCS5Padding).
   *
   * @param   data    Data to decrypt
   * @param   key     Decryption key
   * @return          Result of decryption
   */
  def symDecryptProcessing(data: Array[Byte], key: Array[Byte]): Array[Byte] = {

    // initialize encryption key
    val decryptionKey = new SecretKeySpec(key, "AES")

    // initialize cypher
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val initVec = computeHash("asfasfsafsaf".getBytes).drop(16)
    val initVecSpec = new IvParameterSpec(initVec)
    cipher.init(Cipher.DECRYPT_MODE, decryptionKey, initVecSpec)

    // do encryption
    cipher.doFinal(data)
  }

  /**
   * Generates a random public-private key pair for RSA (1024 bit)
   *
   * Note: This function is randomized but not deterministic.
   *
   * NOTE: BECAUSE DEVELOPMENT AND DEBUGGING WITH SHORTER KEYS IS MORE CONVENIENT, THE ALGORITHM IS CHANGED TO RSA 512 BIT!
   *       !!! CHANGE THIS ON PRODUCTION MODE !!!
   *
   * @return   (public key, private key) - base64 encoded.
   */
  def asymKeyGenerator(): (String, String) = {

    // initialize RSA 1024 bit key pair generator
    val generator = KeyPairGenerator.getInstance("RSA")
    //generator.initialize(1024)
    generator.initialize(512) // TODO --- change to 1024 on production mode...

    // generate key
    val keyPair = generator.generateKeyPair()

    // return public and private key as base64
    val publicKey = byteToString(keyPair.getPublic.getEncoded)
    val privateKey = byteToString(keyPair.getPrivate.getEncoded)
    (publicKey, privateKey)

  }

  /**
   * Generates a symmetric key for AES encryption of length 256 bit or 32 byte.
   *
   * NOTE: To fully utilize AES-256, s:String should at least encode 256 bits even though more or less bits are accepted.
   * NOTE: Because s:String is base64 encoded, not all characters may occur. A–Z, a–z, 0–9, '+' and '/' are valid while last few characters might be '=' (for padding)
   * NOTE: Because s:String is base64 encoded, an input of length 32 provides less than 256 bits. Solution: Use stringToBytes(..) and bytesToString(..)
   * NOTE: It is veeery unlikely that different s:String will generate the same output.
   *
   * @param    s    Parameter to determine output (base64 encoded)
   * @return        DES key of length 256 bit (base64 encoded)
   */
  def symKeyGenerator(s:String): String = {
    // TODO - warning if "s" encodes less than 256 bit
    computeHash(s)
  }

}