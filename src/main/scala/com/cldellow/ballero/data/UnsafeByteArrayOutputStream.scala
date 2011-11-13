package com.cldellow.ballero.data

class UnsafeByteArrayOutputStream(size: Int) extends java.io.ByteArrayOutputStream(size) {
  def unsafe(): Array[Byte] = buf
}

// vim: set ts=2 sw=2 et:
