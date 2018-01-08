package lu.intech.ethkyc.orely.controllers

import java.util.Base64
import javax.inject.{Inject, Singleton}

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.json.FinatraObjectMapper
import com.typesafe.config.{Config, ConfigFactory}
import lu.intech.ethkyc.orely.services.OrelyService

@Singleton
class OrelyController @Inject() (orely:OrelyService, objectMapper: FinatraObjectMapper) extends Controller {
  case class OrelyServiceError(msg:String)

  private val config: Config = ConfigFactory.load()

  get("/request") { req:Request =>
    req.getParam("address") match {
      case address if address == null || address.length == 0 =>
        response.badRequest(OrelyServiceError(msg = "Query param \"address\" is mandatory")).toFutureException
      case address =>
        val toBeCertified = if (address.startsWith("0x")) address.substring(2) else address
        orely.buildSAMLSignatureRequest(toBeCertified, req.getParam("redirect"))
    }
  }

  post("/response") { req:Request =>
    val samlResponse = req.getParam("SAMLResponse")
    println("Response:"+samlResponse)
    val json = objectMapper.writeValueAsString(orely.parseSAMLSignatureResponse(samlResponse))
    val encoded = Base64.getEncoder.encodeToString(json.getBytes("UTF-8"))
    val redirectUrl =
      (req.getParam("redirect") match {
        case null => config.getString("redirectURL")
        case str => str
      }) + "#response=" + encoded
    response.ok(
      s"""<!DOCTYPE html>
        |<html>
        |<head>
        |   <meta http-equiv="refresh" content="0; url=$redirectUrl">
        |</head>
        |<body>
        |   <p>You will be redirected, please wait...</p>
        |   <p>If the redirection doesn't work, please <a href="$redirectUrl">click here.</a></p>
        |</body>
        |</html>""".stripMargin
    ).contentType("text/html; charset=utf-8")
  }


}