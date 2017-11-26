package lu.intech.ethkyc.orely

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter
import lu.intech.ethkyc.orely.controllers.OrelyController

class OrelySvcServer extends HttpServer {

  override val disableAdminHttpServer: Boolean = true
  override val defaultFinatraHttpPort: String = ":8080"


  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[CommonFilters]
      .add[CorsFilter, OrelyController]
  }

}

object OrelySvcServerMain extends OrelySvcServer
