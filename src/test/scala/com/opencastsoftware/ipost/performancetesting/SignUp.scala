package com.opencastsoftware.ipost.performancetesting

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class SignUp extends Simulation {

	val httpProtocol = http
		.baseURL("https://app.ipostsoftware.com")
		.inferHtmlResources()
		.acceptHeader("image/png, image/svg+xml, image/jxr, image/*;q=0.8, */*;q=0.5")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-GB")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063")

	val headers_0 = Map("Accept" -> "text/html, application/xhtml+xml, image/jxr, */*")

	val headers_1 = Map("Accept" -> "application/javascript, */*;q=0.8")

    val uri1 = "https://app.ipostsoftware.com:443"
    val uri2 = "https://cdn.polyfill.io/v2/polyfill.js"

	val scn = scenario("SignUp")
		.exec(http("request_0")
			.get("/?signup=true&me=true")
			.headers(headers_0)
			.resources(http("request_1")
			.get("/2.chunk.b2593.js")
			.headers(headers_1),
            http("request_2")
			.get("/route-signup.chunk.20f68.js")
			.headers(headers_1),
            http("request_3")
			.get("/assets/svgs/logo-ipost.svg"),
            http("request_4")
			.get("/assets/svgs/icon-eye-show.svg")))

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}