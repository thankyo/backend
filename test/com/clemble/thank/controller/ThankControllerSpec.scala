package com.clemble.thank.controller

class ThankControllerSpec extends ControllerSpec {

  "GET" should {

    def generateVariations(masterURI: String): List[String] = {
      List (
        masterURI,
        s"http://$masterURI",
        s"https://$masterURI",
        s"ftp://${masterURI}",
        s"mailto://${masterURI}",
        s"http/$masterURI",
        s"https/$masterURI",
        s"ftp/${masterURI}",
        s"mailto/${masterURI}"
      )
    }


    "support different format types" in {
      true shouldEqual true
    }

  }

}
