package lu.intech.ethkyc.orely.controllers

import javax.inject.{Inject, Singleton}

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import lu.intech.ethkyc.orely.services.OrelyService

case class OrelyResponse(samlRequest: String)

@Singleton
class OrelyController @Inject() (orely:OrelyService) extends Controller {

  get("/request") { _:Request =>
    OrelyResponse(
      samlRequest = orely.buildSAMLRequest()
    )
  }

  post("/response") { req:Request =>
    println(req.contentString)
    req.contentString
  }

}