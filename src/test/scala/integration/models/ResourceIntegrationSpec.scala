package integration.models

import java.io.File

import org.mockftpserver.fake.filesystem.{DirectoryEntry, FileEntry, UnixFakeFileSystem}
import org.mockftpserver.fake.{FakeFtpServer, UserAccount}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll

class ResourceIntegrationSpec extends Specification with BeforeAfterAll {
  val fakeFtpServer = new FakeFtpServer()
  val fileSystem = new UnixFakeFileSystem()
  val account = new UserAccount("user", "password", "/")

  "HTTP Resource" >> {

    "should download and write files using HTTP" >> {
      val fileResource = models.HttpFileResource("unec.edu.az/application/uploads/2014/12/pdf-sample.pdf")
      fileResource.write("target/tmp")
      Thread.sleep(500)
      new File("target/tmp/pdf-sample.pdf").exists() mustEqual true
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
  }
}
