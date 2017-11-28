package com.opencastsoftware.ipost.performancetesting

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class Login extends Simulation {

	val httpProtocol = http
		.baseURL("https://app.ipostsoftware.com:8080/")
		.inferHtmlResources()
		.acceptHeader("application/javascript, */*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-GB")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063")

//	val headers_0 = Map("Accept" -> "text/html, application/xhtml+xml, image/jxr, */*")
//
//	val headers_1 = Map("Accept" -> "text/css, */*")
//
//	val headers_5 = Map("Accept" -> "image/png, image/svg+xml, image/jxr, image/*;q=0.8, */*;q=0.5")
//
//    val uri1 = "https://app.ipostsoftware.com:443"
//    val uri2 = "https://fonts.googleapis.com:443/css"

	val scn = scenario("Login")

  object Login {
    val login = exec(http("Log in")
      .post("uaa/oauth/token")
      .formParam("username", "kitabird@gmail.com")
      .formParam("password", "Password1")
      .formParam("grant_type", "password")
      .formParam("client_id", "ipost")
      // store auth token
      )
  }

  object Upload {
    val upload = exec(http("Create envelope")
      .post("ipostsaas-order-composite-service/envelope")
        .formParam("username", "kitabird@gmail.com")
        .header("Content-Type", "application/json"))
    // .header("Authorization", "Bearer " + authToken)
/*      .exec(http("Upload document")
      .post("ipostsaas-order-composite-service/envelope/" + envelopeId + "/document")
      .formParam("myKey", "myValue")
      .formUpload("file", "standardletter.pdf")
      .formUpload("file", "standardletter.pdf"))*/
  }

  val user = scenario("User").exec(Login.login, Upload.upload)
	setUp(user.inject(atOnceUsers(1))).protocols(httpProtocol)
  //setUp(user.inject(rampUsers(50) over (20 seconds))).protocols(httpProtocol)
}