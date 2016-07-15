package integration.services

import java.io.File

import org.mockftpserver.fake.{FakeFtpServer, UserAccount}
import org.mockftpserver.fake.filesystem.{DirectoryEntry, FileEntry, UnixFakeFileSystem}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import parsers.ResourceUrlParser
import services.ResourceCopyService

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ResourceCopyServiceIntegrationSpec extends Specification with BeforeAfterAll {
  val fakeFtpServer = new FakeFtpServer()
  val fileSystem = new UnixFakeFileSystem()
  val account = new UserAccount("user", "password", "/")

  "Resource Copy Service" >> {
    val resourceUrlParser = new ResourceUrlParser
    val resourceCopyService = new ResourceCopyService(resourceUrlParser)

    "should copy a list of resources into destination directory" >> {
      val resources = List(
        "ftp://user:password@127.0.0.1:20000/data/some-ftp-file.txt",
        "http://unec.edu.az/application/uploads/2014/12/pdf-sample.pdf",
        s"file://${new File("src/test/resources/local-file.txt").getAbsolutePath}"
      )
      val destinationDirectory = "target/tmp"
      val future = resourceCopyService.copyResources(resources, destinationDirectory)

      Thread.sleep(500)
      Await.result(future, Duration.Inf) mustEqual List("done", "done", "done")
      new File("target/tmp/pdf-sample.pdf").exists() mustEqual true
      new File("target/tmp/some-ftp-file.txt").exists() mustEqual true
      new File("target/tmp/local-file.txt").exists() mustEqual true
    }
  }

  override def beforeAll(): Unit = {
    val tmpDirectory = new File("target/tmp")
    if(!tmpDirectory.exists()) tmpDirectory.mkdir()
    else tmpDirectory.listFiles().map(_.delete())

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
  }
}
