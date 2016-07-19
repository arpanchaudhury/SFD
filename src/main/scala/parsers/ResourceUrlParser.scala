package parsers

import exceptions.ParseException
import models.{FtpFileResource, HttpFileResource, LocalFileResource, SftpFileResource}

class ResourceUrlParser {

  def parse(url: String) = url match {
    case `url` if url.startsWith("ftp://")   => FtpFileResource(url.replace("ftp://", ""))
    case `url` if url.startsWith("sftp://")  => SftpFileResource(url.replace("sftp://", ""))
    case `url` if url.startsWith("http://")  => HttpFileResource(url.replace("http://", ""))
    case `url` if url.startsWith("https://") => HttpFileResource(url.replace("https://", ""))
    case `url` if url.startsWith("file://")  => LocalFileResource(url.replace("file://", ""))
    case others                              => throw ParseException("Error: Illegal resource url")
  }
}
