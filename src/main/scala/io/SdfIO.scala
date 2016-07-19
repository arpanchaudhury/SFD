package io

class SdfIO {

  def getDestinationDirectory = {
    println("""
      |Enter the destination directory of file to be downloaded followed by enter.
      """.stripMargin)
    scala.io.StdIn.readLine()
  }

  def getUrls = {
    println(
      """
        |Enter the comma separated resource urls you want to download followed by enter.
        """.stripMargin)
    scala.io.StdIn.readLine().split(",").map(_.trim).toList
  }

  def printWelcomeMessage() = println(
    """
      |Welcome to simple file downloader
      |---------------------------------
      |Please enter the destination folder and list of comma separate resource urls to download as prompted.
      |
    """.stripMargin)

  def printExitMessage() = {
    println(
      """
        |Files resources successfully downloaded...
        |
        |Thank you for using this application. Press enter to exit.
        |If you are using `docker-compose run application` take a look at docker container file system for downloaded files before exit.
        |Use docker exec -i -t <container-id> bash to get bash shell of created docker container.
        |""".stripMargin)
    scala.io.StdIn.readLine()
  }
}
