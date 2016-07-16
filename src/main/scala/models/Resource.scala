package models

trait Resource

case class LocalFileResource(url: String) extends Resource

case class FtpFileResource(url: String) extends Resource

case class HttpFileResource(url: String) extends Resource

case class SftpFileResource(url: String) extends Resource
