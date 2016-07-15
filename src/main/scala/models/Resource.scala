package models

import java.io.{File, FileOutputStream}
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}

import exceptions.{FileNotFoundException, IncorrectCredentialsException, ParseException}
import org.apache.commons.net.ftp.FTPClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.sys.process._

trait Resource {
  def write(path: String): Future[Any]
}

case class LocalFileResource(url: String) extends Resource {

  override def write(path: String): Future[Any] = {
    val fileName = url.substring(url.lastIndexOf('/'))
    Future {
      if (new File(url).exists)
        Files.copy(new File(url).toPath, new File(path + fileName).toPath, StandardCopyOption.COPY_ATTRIBUTES)
      else
        throw FileNotFoundException("[Error]: No file found")
    } recover {
      case exception =>
        val file = new File(path + fileName)
        if (file.exists) file.delete()
    }
  }
}

case class FtpFileResource(url: String) extends Resource {

  override def write(path: String): Future[Any] = {
    val fileName = url.substring(url.lastIndexOf('/'))
    Future {
      val ftpClient = new FTPClient()
      val ftpUrl = parse(url)

      ftpClient.connect(ftpUrl.host, if(ftpUrl.port.isDefined) ftpUrl.port.get else 21)

      if(ftpClient.login(ftpUrl.username, ftpUrl.password)) {
        val outputStream = new FileOutputStream(path + fileName)
        if(!ftpClient.retrieveFile(ftpUrl.path, outputStream)) {
          outputStream.close()
          ftpClient.disconnect()
          throw FileNotFoundException("[Error]: File not found")
        }
        outputStream.close()
        ftpClient.disconnect()
      } else {
        throw IncorrectCredentialsException("[Error]: Incorrect username and password")
      }
    } recover {
      case exception =>
        val file = new File(path + fileName)
        if (file.exists) file.delete()
    }
  }

  private case class FtpUrl(host: String, port: Option[Int], username: String, password: String, path: String)

  private def parse(ftpUrl: String) = {
    val ftpUrlPattern = "(\\w+):(\\w+)@([.0-9]+):(\\d+)(.*)".r
    val ftpUrlPatternDefaultPort = "(\\w+):(\\w+)@([.0-9]+)(.*)".r

    url match {
      case ftpUrlPattern(username, password, host, port, path) => FtpUrl(host, Some(port.toInt), username, password, path)
      case ftpUrlPatternDefaultPort(username, password, host, path) => FtpUrl(host, None, username, password, path)
      case _ => throw ParseException("[Error]: FTP Url parse exception")
    }
  }
}

case class HttpFileResource(url: String) extends Resource {

  override def write(path: String): Future[Any] = {
    val fileName = url.substring(url.lastIndexOf('/'))
    Future {
      new URL("http://" + url) #> new File(path + fileName) !!
    } recover {
      case exception =>
        val file = new File(path + fileName)
        if (file.exists) file.delete()
    }
  }
}

case class SftpFileResource(url: String) extends Resource {
  override def write(path: String): Future[Any] = ???
}
