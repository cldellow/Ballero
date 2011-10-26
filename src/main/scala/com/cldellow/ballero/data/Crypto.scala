package com.cldellow.ballero.data

// Java
import java.security._
import javax.crypto._
import javax.crypto.spec._
import java.text.SimpleDateFormat
import java.util.Date


// 3rd party
import org.coriander.oauth._
import org.coriander.oauth.core._
import org.coriander.oauth.core.cryptography._

/** Wrappers for crypto functions */
object Crypto {
  private val consumer = new Consumer(new Credential(Keys.key, Keys.secret))
  def sign(url: String): String = {
    val uri = new java.net.URI(url)
    consumer.sign(uri).toString
  }

  private def hash(input: String): Array[Byte] = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.reset()
    digest.digest(input.getBytes("UTF-8"))
 }

  lazy val aesKey: Array[Byte] = hash(Keys.secret)

  /** Returns a base64 encoded AES256 encrypted version of cleartext using the SHA-256
      hash of your OAuth secret as a key. */
  def aes256(cleartext: String): String = {
    val secretKeySpec = new SecretKeySpec(aesKey, "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE,
      secretKeySpec,
      new IvParameterSpec((for (i <- 1 to 16) yield 0:Byte).toArray))
    val encrypted = cipher.doFinal(cleartext.getBytes("UTF-8"))
    Base64.encode(encrypted)
  }

  def iso8601: String = {
    val now = new Date()
    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000Z")
    sdf.format( now )
  }

  private def encode(str: String): String = java.net.URLEncoder.encode(str, "UTF-8")

  def appsign(url: String, params: Map[String, String]): String = {
    val timestamp = iso8601
    val paramsToSign = params ++ Map("access_key" -> Keys.key, "timestamp" -> timestamp)
    val proveMe = "%s?%s".format(url, paramsToSign.keySet.toList.sorted
      .map { name => (name, paramsToSign(name)) }
      .map { case (name, value) => "%s=%s".format(encode(name), encode(value)) }.mkString("&")
    )


    val hmac256 = new HmacSha256
    val signature = Base64.encode(hmac256.create(Keys.secret, proveMe))
    "%s&signature=%s".format(proveMe, encode(signature))
  }
}

// vim: set ts=2 sw=2 et:
