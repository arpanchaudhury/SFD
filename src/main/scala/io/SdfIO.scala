package io

class SdfIO {

  def getDestinationDirectory = {
    println("Enter the destination directory of file to be downloaded followed by enter")
    scala.io.StdIn.readLine()
  }

  def getUrls = {
    println("Enter the comma separated resource urls you want to download followed by enter")
    scala.io.StdIn.readLine().split(",").map(_.trim).toList
  }

  def printWelcomeMessage() = println(
    """
      |Welcome to simple file downloader
      |---------------------------------
      |Please enter the destination folder and list of comma separate resource urls to download as prompted
      |
      |
    """.stripMargin)
}
