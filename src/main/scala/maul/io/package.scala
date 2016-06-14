package maul

import java.io._
import java.util.zip._
import scala.io.Source.{ fromFile, fromInputStream }
import scala.io.BufferedSource

package object io {

  def createWriter(filename: String): Writer =
    createWriter(new File(filename))

  def createWriter(parentDir: File, filename: String): Writer =
    createWriter(new File(parentDir, filename))

  def createWriter(file: File): Writer =
    if (isgz(file)) gzipWriter(file) else new BufferedWriter(new FileWriter(file))

  def createDataOutputStream(parentDir: File, filename: String): DataOutputStream =
    new DataOutputStream(new GZIPOutputStream(
      new FileOutputStream(new File(parentDir, filename))))

  def gzipWriter(file: File): BufferedWriter =
    new BufferedWriter(new OutputStreamWriter(
      new GZIPOutputStream(new FileOutputStream(file))))

  def getSource(file: String): BufferedSource =
    getSource(new File(file))

  def getSource(file: File): BufferedSource =
    if (isgz(file)) gzipSource(file) else fromFile(file)

  def gzipSource(file: String): BufferedSource =
    gzipSource(new File(file))

  def gzipSource(file: File): BufferedSource = {
    fromInputStream(new GZIPInputStream(new FileInputStream(file)))
  }

  def isgz(file: File): Boolean =
    file.getName.endsWith(".gz")

  /**
    * Create a temporary file, with reasonable defaults for suffix and base dir.
    */
    def tmpFile(prefix: String, suffix: String = ".txt", baseDir: String = "/tmp") =
        java.io.File.createTempFile(prefix, suffix, new java.io.File(baseDir))

}
