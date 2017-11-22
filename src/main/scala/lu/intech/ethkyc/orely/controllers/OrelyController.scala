package lu.intech.ethkyc.orely.controllers

import java.security.MessageDigest
import javax.inject.{Inject, Singleton}

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import lu.intech.ethkyc.orely.services.OrelyService
import org.apache.commons.codec.binary.Hex

@Singleton
class OrelyController @Inject() (orely:OrelyService) extends Controller {

  case class OrelyServiceError(msg:String)

  get("/request") { req:Request =>
    req.getParam("address") match {
      case address if address == null || address.length == 0 =>
        response.badRequest(OrelyServiceError(msg = "Query param \"address\" is mandatory")).toFutureException
      case address =>
        val toBeCertified = if (address.startsWith("0x")) address.substring(2) else address
        val binAddress = Hex.decodeHex(toBeCertified.toCharArray)
        val hash = MessageDigest.getInstance("SHA-256").digest(binAddress)
        orely.buildSAMLSignatureRequest(hash)
    }
  }

  post("/response") { req:Request =>
    val samlResponse = req.getParam("SAMLResponse")
    orely.parseSAMLSignatureResponse(samlResponse)
  }

}