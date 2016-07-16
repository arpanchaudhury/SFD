package integration.models

import java.io.File

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll

class ResourceIntegrationSpec extends Specification with BeforeAfterAll {

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

  override def beforeAll(): Unit = {
    val tmpDirectory = new File("target/tmp")
    if(!tmpDirectory.exists()) tmpDirectory.mkdir()
  }

  override def afterAll(): Unit = {
    val tmpDirectory = new File("target/tmp")
    if(tmpDirectory.exists()) {
      tmpDirectory.listFiles().map(_.delete())
      tmpDirectory.delete()
    }
  }
}
