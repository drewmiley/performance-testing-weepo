package com.opencastsoftware.ipost.performancetesting

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class CreateAndSubmitEnvelope extends Simulation {

	val httpProtocol = http
		.baseURL("https://app.ipostsoftware.com:8080/")
		.inferHtmlResources()
		.acceptHeader("application/javascript, */*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-GB")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063")

	val headers_0 = Map("Content-Type" -> "application/json", "Authorization" -> "Bearer ${accessToken}")

	val login = exec(http("Log in")
		.post("uaa/oauth/token")
		.formParam("username", "kitabird@gmail.com")
		.formParam("password", "Password1")
		.formParam("grant_type", "password")
		.formParam("client_id", "ipost")
		.check(jsonPath("$.access_token").saveAs("accessToken")))

	val createEnvelope = exec(http("Create envelope")
		.post("ipostsaas-order-composite-service/envelope")
		.body(StringBody("""{"username":"kitabird@gmail.com"}"""))
		.headers(headers_0)
		.check(bodyString.saveAs("envelopeId")))

	val uploadDocument = exec(http("Upload document")
		.post("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "/document")
		.formParam("file", "standardletter.pdf")
		.formUpload("file", "standardletter.pdf"))

	val getEnvelope = exec(http("Get Envelope")
		.get("ipostsaas-order-composite-service/envelope/" + "${envelopeId}")
		.headers(headers_0)
  	.check(jsonPath("$").saveAs("envelope")))

	val getDocumentId = exec(http("Get Document Id")
		.get("ipostsaas-order-composite-service/envelope/" + "${envelopeId}")
		.headers(headers_0)
		.check(jsonPath("$.documents[0].id").saveAs("documentId")))

	val getDocumentId2 = exec(http("Get Second Document Id")
		.get("ipostsaas-order-composite-service/envelope/" + "${envelopeId}")
		.headers(headers_0)
		.check(jsonPath("$.documents[1].id").saveAs("documentId2")))

	val addressScan = exec(http("Scan for address")
		.get("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "/document/" + "${documentId}")
		.headers(headers_0))

	val getPrice = exec(http("Get price")
		.get("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "/price/coversheet")
		.headers(headers_0))

  val addRate = exec(http("Add rate")
    .patch("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "/rate/ECONOMY")
    .headers(headers_0))

  val addRecipientAddress = exec(http("Add recipient address")
    .put("ipostsaas-order-composite-service/envelope/" + "${envelopeId}")
    .body(StringBody("""{"recipientAddress":{"addressLine1":"Walker Road", "postTown": "Newcastle Upon Tyne", "postcode": "NE6 2HL"}}"""))
    .headers(headers_0))

  val addReturnAddress = exec(http("Add return address")
    .put("ipostsaas-order-composite-service/envelope/" + "${envelopeId}")
    .body(StringBody("""{"returnAddress":{"addressLine1":"Walker Road", "postTown": "Newcastle Upon Tyne", "postcode": "NE6 2HL"}}"""))
    .headers(headers_0))

  val sendEnvelope = exec(http("Send envelope")
    .put("ipostsaas-order-composite-service/envelope/send/" + "${envelopeId}" + "/submit")
    .headers(headers_0))

	val setToDoubleSided = exec(http("Set to double sided")
  	.patch("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "/document/" + "${documentId}" + "/doublesided/true")
		.headers(headers_0))

	val reorderDocuments = exec(http("Reorder documents")
  	.patch("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "?documentid=" + "${documentId}" + "&documentid=" + "${documentId2}"
			+ "&order=2&order=1")
		.headers(headers_0))

	val deleteDocument = exec(http("Delete document")
  	.delete("ipostsaas-order-composite-service/envelope/" + "${envelopeId}" + "/document/" + "${documentId}")
		.headers(headers_0))

  val user = scenario("User").exec(login, createEnvelope, uploadDocument, getDocumentId, addressScan, setToDoubleSided,
		addRate, addRecipientAddress, addReturnAddress, getPrice, uploadDocument, getDocumentId2, reorderDocuments, deleteDocument, sendEnvelope)
//	The line below is for testing out newly written scenarios
	setUp(user.inject(atOnceUsers(1))).protocols(httpProtocol)
//	The line below is for full performance testing purposes
//  setUp(user.inject(rampUsers(50) over (20 seconds))).protocols(httpProtocol)
}