package integration.services

import java.io.File

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import parsers.ResourceUrlParser
import services.ResourceCopyService

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ResourceCopyServiceIntegrationSpec extends Specification with BeforeAfterAll {

  "Resource Copy Service" >> {
    val resourceUrlParser = new ResourceUrlParser
    val resourceCopyService = new ResourceCopyService(resourceUrlParser)

    "should copy a list of resources into destination directory" >> {
      val resources = List(
        "http://unec.edu.az/application/uploads/2014/12/pdf-sample.pdf",
        s"file://${new File("src/test/resources/local-file.txt").getAbsolutePath}"
      )
      val destinationDirectory = "target/tmp"
      val future = resourceCopyService.copyResources(resources, destinationDirectory)

      Await.result(future, Duration.Inf) mustEqual List("done", "done")
      new File("target/tmp/pdf-sample.pdf").exists() mustEqual true
      new File("target/tmp/local-file.txt").exists() mustEqual true
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
