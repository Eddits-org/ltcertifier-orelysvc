package lu.intech.ethkyc.orely.services

import java.io.{File, FileInputStream}
import java.net.URL
import java.security.KeyStore
import javax.inject.Singleton

import com.typesafe.config.{Config, ConfigFactory}
import lu.luxtrust.orely.api.factory.SAMLCredentialFactory
import lu.luxtrust.orely.api.saml.SAMLCredential
import lu.luxtrust.orely.api.service.{CertificateRequest, RequestParameters, RequestService}

@Singleton
class OrelyService {

  private val config: Config = ConfigFactory.load()

  private val credential: SAMLCredential = {
    val ks = KeyStore.getInstance("JKS")
    val input =
      if (config.hasPath("credentials.keystore.classpath"))
        classOf[OrelyService].getResourceAsStream(config.getString("credentials.keystore.classpath"))
      else
        new FileInputStream(new File(config.getString("credentials.keystore.path")))

    ks.load(input, config.getString("credentials.keystorePassword").toCharArray)
    SAMLCredentialFactory.createSAMLCredential(ks, config.getString("credentials.keyAlias"), config.getString("credentials.keyPassword"))
  }

  def buildSAMLRequest(): String = {
    val params = new RequestParameters()
    params.setCertificateRequest(CertificateRequest.REQUIRED)
    val svc = new RequestService(
      credential,
      new URL(config.getString("luxtrust.destinationURL")),
      new URL(config.getString("luxtrust.returnURL")),
      new URL(config.getString("luxtrust.issuerURL")),
      config.getBoolean("luxtrust.ocsp")
    )
    svc.createRequest(params).replaceAll("\\n", "")
  }

}
