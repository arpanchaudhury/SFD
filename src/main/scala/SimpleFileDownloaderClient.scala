import io.SdfIO
import parsers.ResourceUrlParser
import services.ResourceCopyService

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SimpleFileDownloaderClient extends App {
  override def main(args: Array[String]): Unit = {
    val stdIO = new SdfIO
    val resourceUrlParser = new ResourceUrlParser
    val resourceCopyService = new ResourceCopyService(resourceUrlParser)

    stdIO.printWelcomeMessage()
    val destinationDirectory = stdIO.getDestinationDirectory
    val resourceUrls = stdIO.getUrls

    val process = resourceCopyService.copyResources(resourceUrls, destinationDirectory)

    Await.result(process, Duration.Inf)

    println("files resources successfully downloaded...")
  }
}
