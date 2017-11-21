package lu.intech.ethkyc.orely.controllers

import java.security.MessageDigest
import javax.inject.{Inject, Singleton}

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import lu.intech.ethkyc.orely.services.OrelyService
import org.apache.commons.codec.binary.Hex

@Singleton
class OrelyController @Inject() (orely:OrelyService) extends Controller {

  get("/request") { _:Request =>
    val address = Hex.decodeHex("98f408cdb75481d95a124be5e5cc60c0d11afcab".toCharArray)
    val hash = MessageDigest.getInstance("SHA-256").digest(address)
    orely.buildSAMLSignatureRequest(hash)
  }

  post("/response") { req:Request =>
    val samlResponse = req.getParam("SAMLResponse")
    orely.parseSAMLSignatureResponse(samlResponse)
  }

}