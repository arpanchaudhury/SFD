package services

import java.io.File

import exceptions.IllegalDestinationDirectoryException
import parsers.ResourceUrlParser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ResourceCopyService(resourceUrlParser: ResourceUrlParser) {

  def copyResources(resources: List[String], path: String) = {
    val resourceDestinationDirectory = new File(path)
    val status =
      if(resourceDestinationDirectory.exists() && resourceDestinationDirectory.isDirectory) {
        resources.map(resourceUrlParser.parse).map(_.write(path))
      }
      else if(resourceDestinationDirectory.exists() && resourceDestinationDirectory.isFile) {
        throw IllegalDestinationDirectoryException(s"$path is not a directory.")
      }
      else {
        resourceDestinationDirectory.mkdir()
        resources.map(resourceUrlParser.parse).map(_.write(path))
      }
    Future.traverse(status)(_ => Future("done"))
  }
}
