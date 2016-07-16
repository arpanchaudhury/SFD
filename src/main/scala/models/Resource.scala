package models

import java.io.File
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}

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
      Files.copy(new File(url).toPath, new File(path + fileName).toPath, StandardCopyOption.COPY_ATTRIBUTES)
    } recover {
      case exception =>
        val file = new File(path + fileName)
        if (file.exists) file.delete()
    }
  }
}

case class FtpFileResource(url: String) extends Resource {
  override def write(path: String): Future[Any] = ???
}

case class HttpFileResource(url: String) extends Resource {

  override def write(path: String): Future[Any] = {
    val fileName = url.substring(url.lastIndexOf('/'))
    Future {
      new URL("http://" + url) #> new File(path + fileName) !!
    }  recover {
      case exception =>
        val file = new File(path + fileName)
        if (file.exists) file.delete()
    }
  }
}

case class SftpFileResource(url: String) extends Resource {
  override def write(path: String): Future[Any] = ???
}
