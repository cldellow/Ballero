package com.cldellow.ballero.data

// Java
import java.security._
import javax.crypto._
import javax.crypto.spec._
import java.text.SimpleDateFormat
import java.util.Date

// android
import android.util.Log


// 3rd party
import org.coriander.oauth._
import org.coriander.oauth.core._
import org.coriander.oauth.core.cryptography._

/** Wrappers for crypto functions */
object Crypto {
  def getSignedParams(url: String, params: Map[String, String], secret: String): Map[String, String] = {
    val proveMe = getProveMe(url, params)
    val hmac256 = new HmacSha256
    val signature = Base64.encode(hmac256.create(secret, proveMe))

    params ++ List("signature" -> signature)
  }

  def getProveMe(url: String, params: Map[String, String]): String =
    "%s?%s".format(url, params.keySet.toList.sorted
      .map { name => (name, params(name)) }
      .map { case (name, value) => "%s=%s".format(encode(name), encode(value)) }.mkString("&")
    )

  private def finalsign(url: String, params: Map[String, String], secret: String): String = {
    val proveMe = getProveMe(url, params)

    val hmac256 = new HmacSha256
    val signature = Base64.encode(hmac256.create(secret, proveMe))
    "%s&signature=%s".format(proveMe, encode(signature))
  }

  private def commonParams: Map[String, String] =
    Map("access_key" -> Keys.key, "timestamp" -> iso8601)

  /** Sign a request as a user */
  def sign(url: String, params: Map[String, String], token: String, secret: String): String = {
    val paramsToSign = params ++ commonParams ++ Map("auth_token" -> token)
    finalsign(url, paramsToSign, secret)
  }

  def signParams(url: String, params: Map[String, String], token: String, secret: String): Map[String, String] = {
    val paramsToSign = params ++ commonParams ++ Map("auth_token" -> token)
    getSignedParams(url, paramsToSign, secret)
  }

  /** Sign a request as Ballero */
  def appsign(url: String, params: Map[String, String]): String = {
    val paramsToSign = params ++ commonParams
    finalsign(url, paramsToSign, Keys.secret)
  }

  def appSignParams(url: String, params: Map[String, String]): Map[String, String] = {
    val paramsToSign = params ++ commonParams
    getSignedParams(url, paramsToSign, Keys.secret)
  }


  private def hash(input: String): Array[Byte] = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.reset()
    digest.digest(input.getBytes("UTF-8"))
 }

  lazy val aesKey: Array[Byte] = hash(Keys.secret)

  /** Returns a base64 encoded AES256 encrypted version of cleartext using the SHA-256
      hash of your OAuth secret as a key.

      Note: uses CBC with all zeroes initialization vector.
  */
  def aes256(cleartext: String): String = {
    val secretKeySpec = new SecretKeySpec(aesKey, "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE,
      secretKeySpec,
      new IvParameterSpec((for (i <- 1 to 16) yield 0:Byte).toArray))
    val encrypted = cipher.doFinal(cleartext.getBytes("UTF-8"))
    Base64.encode(encrypted)
  }

  /** Gets an ISO8601 formatted timestamp. */
  def iso8601: String = {
    val now = new Date()
    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000Z")
    sdf.format( now )
  }

  def encode(str: String): String = java.net.URLEncoder.encode(str, "UTF-8")
}

// vim: set ts=2 sw=2 et:
