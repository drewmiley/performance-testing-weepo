package com.opencastsoftware.ipost.performancetesting

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class IPostTesting extends Simulation {

	val httpProtocol = http
		.baseURL("https://app.ipostsoftware.com:8080/")
		.inferHtmlResources()
		.acceptHeader("application/javascript, */*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-GB")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063")

	val login = exec(http("Log in")
		.post("uaa/oauth/token")
		.formParam("username", "kitabird@gmail.com")
		.formParam("password", "Password1")
		.formParam("grant_type", "password")
		.formParam("client_id", "ipost")
		.check(jsonPath("$.access_token").saveAs("accessToken")))

	val createAndUpload = exec(http("Create envelope")
		.post("ipostsaas-order-composite-service/envelope")
		.body(StringBody("""{"username":"kitabird@gmail.com"}"""))
		.header("Content-Type", "application/json")
		.header("Authorization", "Bearer " + "${accessToken}")
		.check(bodyString.saveAs("envelopeId")))
			.exec(http("Upload document")
			.post("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "/document")
			.formParam("file", "standardletter.pdf")
			.formUpload("file", "standardletter.pdf"))

	val getEnvelope = exec(http("Get Envelope")
		.get("ipostsaas-order-composite-service/envelope/" + "${envelopeId}")
		.header("Content-Type", "application/json")
		.header("Authorization", "Bearer " + "${accessToken}")
  	.check(jsonPath("$").saveAs("envelope")))

	val getDocumentId = exec(http("Get Document Id")
		.get("ipostsaas-order-composite-service/envelope/" + "${envelopeId}")
		.header("Content-Type", "application/json")
		.header("Authorization", "Bearer " + "${accessToken}")
		.check(jsonPath("$.documents[0].id").saveAs("documentId")))

	val addressScan = exec(http("Scan for address")
		.get("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "/document/" + "${documentId}")
		.header("Content-Type", "application/json")
		.header("Authorization", "Bearer " + "${accessToken}"))

	val getPrice = exec(http("Get price")
		.get("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "/price/coversheet")
		.header("Content-Type", "application/json")
		.header("Authorization", "Bearer " + "${accessToken}"))

  val addRate = exec(http("Add rate")
    .patch("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "/rate/ECONOMY")
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer " + "${accessToken}"))

  val addRecipientAddress = exec(http("Add recipient address")
    .put("ipostsaas-order-composite-service/envelope/" + "${envelopeId}")
    .body(StringBody("""{"recipientAddress":{"addressLine1":"Walker Road", "postTown": "Newcastle Upon Tyne", "postcode": "NE6 2HL"}}"""))
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer " + "${accessToken}"))

  val addReturnAddress = exec(http("Add return address")
    .put("ipostsaas-order-composite-service/envelope/" + "${envelopeId}")
    .body(StringBody("""{"returnAddress":{"addressLine1":"Walker Road", "postTown": "Newcastle Upon Tyne", "postcode": "NE6 2HL"}}"""))
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer " + "${accessToken}"))

  val sendEnvelope = exec(http("Send envelope")
    .put("ipostsaas-order-composite-service/envelope/send/" + "${envelopeId}" + "/submit")
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer " + "${accessToken}"))

  val user = scenario("User").exec(login, createAndUpload, getDocumentId, addressScan, addRate, addRecipientAddress, addReturnAddress, getPrice, sendEnvelope)
//	The line below is for testing out newly written scenarios
	setUp(user.inject(atOnceUsers(1))).protocols(httpProtocol)
//	The line below is for full performance testing purposes
//  setUp(user.inject(rampUsers(50) over (20 seconds))).protocols(httpProtocol)
}