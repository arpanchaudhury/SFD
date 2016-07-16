package integration.services

import java.io.{BufferedInputStream, File, FileInputStream}
import java.net.InetSocketAddress

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import org.mockftpserver.fake.{FakeFtpServer, UserAccount}
import org.mockftpserver.fake.filesystem.{DirectoryEntry, FileEntry, UnixFakeFileSystem}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import parsers.ResourceUrlParser
import services.ResourceCopyService

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ResourceCopyServiceIntegrationSpec extends Specification with BeforeAfterAll {
  val fakeHttpServer = HttpServer.create(new InetSocketAddress(10000), 0)
  val fakeFtpServer = new FakeFtpServer()
  val fileSystem = new UnixFakeFileSystem()
  val account = new UserAccount("user", "password", "/")

  "Resource Copy Service" >> {
    val resourceUrlParser = new ResourceUrlParser
    val resourceCopyService = new ResourceCopyService(resourceUrlParser)

    "should copy a list of resources into destination directory" >> {
      val resources = List(
        "http://localhost:10000/test/http-file.pdf",
        s"file://${new File("src/test/resources/local-file.txt").getAbsolutePath}",
        "ftp://user:password@127.0.0.1:20000/data/some-ftp-file.txt"
      )
      val destinationDirectory = "target/tmp"
      val future = resourceCopyService.copyResources(resources, destinationDirectory)

      Thread.sleep(500)
      Await.result(future, Duration.Inf) mustEqual List("done", "done", "done")
      new File("target/tmp/http-file.pdf").exists() mustEqual true
      new File("target/tmp/some-ftp-file.txt").exists() mustEqual true
      new File("target/tmp/local-file.txt").exists() mustEqual true
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

    fakeFtpServer.setServerControlPort(20000)
    fakeFtpServer.addUserAccount(account)
    fileSystem.add(new DirectoryEntry("/data"))
    fileSystem.add(new FileEntry("/data/some-ftp-file.txt", "useless file"))
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
