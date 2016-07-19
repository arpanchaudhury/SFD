package parsers

import exceptions.ParseException
import models.{FtpFileResource, HttpFileResource, LocalFileResource, SftpFileResource}
import org.specs2.mutable.Specification

class ResourceUrlParserSpec extends Specification {
  "Resource URL Parser" >> {
    val resourceParser = new ResourceUrlParser

    "should parse local file url" >> {
      resourceParser.parse("file:///some/file/url") mustEqual LocalFileResource("/some/file/url")
    }

    "should parse ftp file url" >> {
      resourceParser.parse("ftp://some/file/url")   mustEqual FtpFileResource("some/file/url")
    }

    "should parse sftp resource" >> {
      resourceParser.parse("sftp://some/file/url")  mustEqual SftpFileResource("some/file/url")
    }

    "should parse http resource" >> {
      resourceParser.parse("http://some/file/url")  mustEqual HttpFileResource("some/file/url")
    }

    "should parse https resource" >> {
      resourceParser.parse("https://some/file/url")  mustEqual HttpFileResource("some/file/url")
    }

    "should throw exception for malformed CanvasCreate command" >> {
      resourceParser.parse("bad url") must throwA[ParseException]
    }
  }
}
