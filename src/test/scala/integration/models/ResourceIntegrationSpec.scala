package integration.models

import java.io.{BufferedInputStream, File, FileInputStream}
import java.net.InetSocketAddress

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import org.apache.sshd.SshServer
import org.mockftpserver.fake.filesystem.{DirectoryEntry, FileEntry, UnixFakeFileSystem}
import org.mockftpserver.fake.{FakeFtpServer, UserAccount}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll

class ResourceIntegrationSpec extends Specification with BeforeAfterAll {
  val fakeHttpServer = HttpServer.create(new InetSocketAddress(20000), 0)
  val fakeFtpServer = new FakeFtpServer()
  val fakeSftpServer = SshServer.setUpDefaultServer()
  val fileSystem = new UnixFakeFileSystem()
  val account = new UserAccount("user", "password", "/")

  "HTTP Resource" >> {

    "should download and write files using HTTP" >> {
      val fileResource = models.HttpFileResource("localhost:20000/test/http-file.pdf")
      fileResource.write("target/tmp")
      Thread.sleep(500)
      new File("target/tmp/http-file.pdf").exists() mustEqual true
    }

    "should not download partial files using HTTP" >> {
      val fileResource = models.HttpFileResource("some/invalid/url/test.txt")
      fileResource.write("target/tmp")
      Thread.sleep(500)
      new File("target/tmp/test.txt").exists() mustEqual false
    }
  }

  "Local Resource" >> {

    "should copy local files" >> {
      val fileResource = models.LocalFileResource("src/test/resources/local-file.txt")
      fileResource.write("target/tmp")
      Thread.sleep(500)
      new File("target/tmp/local-file.txt").exists() mustEqual true
    }

    "should not copy partial local files" >> {
      val fileResource = models.LocalFileResource("some/invalid/url/test.txt")
      fileResource.write("target/tmp")
      Thread.sleep(500)
      new File("target/tmp/test.txt").exists() mustEqual false
    }
  }

  "FTP Resource" >> {

    "should copy files using ftp" >> {
      val fileResource = models.FtpFileResource("user:password@127.0.0.1:10000/data/ftp-file.txt")

      fileResource.write("target/tmp")

      Thread.sleep(500)
      new File("target/tmp/ftp-file.txt").exists() mustEqual true
    }

    "should not copy partial ftp files" >> {
      val fileResource = models.FtpFileResource("user:password@127.0.0.1:10000/data/non-existing-file.txt")

      fileResource.write("target/tmp")

      Thread.sleep(500)
      new File("target/tmp/non-existing-file.txt").exists() mustEqual false
    }
  }

  override def beforeAll(): Unit = {
    val tmpDirectory = new File("target/tmp")
    if(!tmpDirectory.exists()) tmpDirectory.mkdir()
    else tmpDirectory.listFiles().map(_.delete())

    class MockRequestHandler extends HttpHandler {
      override def handle(t: HttpExchange) = {
        val h = t.getResponseHeaders
        h.add("Content-Type", "application/pdf")
        val file = new File ("src/test/resources/http-file.pdf")
        val byteArray = new Array[Byte](file.length.toInt)
        val inputStream = new FileInputStream(file)
        val bufferedInputStream = new BufferedInputStream(inputStream)
        bufferedInputStream.read(byteArray, 0, byteArray.length)
        t.sendResponseHeaders(200, file.length())
        val os = t.getResponseBody
        os.write(byteArray, 0, byteArray.length)
        os.close()
      }
    }

    fakeHttpServer.createContext("/test/http-file.pdf", new MockRequestHandler())
    fakeHttpServer.setExecutor(null)
    fakeHttpServer.start()

    fakeFtpServer.setServerControlPort(10000)
    fakeFtpServer.addUserAccount(account)
    fileSystem.add(new DirectoryEntry("/data"))
    fileSystem.add(new FileEntry("/data/ftp-file.txt", "useless file"))
    fakeFtpServer.setFileSystem(fileSystem)
    fakeFtpServer.start()
  }

  override def afterAll(): Unit = {
    val tmpDirectory = new File("target/tmp")
    if(tmpDirectory.exists()) {
      tmpDirectory.listFiles().map(_.delete())
      tmpDirectory.delete()
    }
    fakeFtpServer.stop()
    fakeHttpServer.stop(0)
  }
}
