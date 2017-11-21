package lu.intech.ethkyc.orely.services

import java.io._
import java.net.URL
import java.security.KeyStore
import java.util.Base64
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

import com.typesafe.config._
import lu.luxtrust.orely.api.factory.SAMLCredentialFactory
import lu.luxtrust.orely.api.saml.SAMLCredential
import lu.luxtrust.orely.api.service._
import lu.luxtrust.dss.client.options.xml._
import lu.luxtrust.dss.client.sign.xml.{XmlSignRequestGenerator, XmlSignResponseParser}
import org.apache.commons.codec.binary.Hex
import org.apache.commons.io.IOUtils
import org.apache.xml.security.signature.XMLSignature

@Singleton
class OrelyService {

  case class OrelySignRequest(samlRequest: String)
  case class OrelySignResult(signedInfo: String, manifest: String, signedInfoSignature: String, signerCertificate: String)

  private val config: Config = ConfigFactory.load()

  private val ethAddressSignatureURI: String = config.getString("ethAddressSignURI")

  private lazy val docBuilderFactory = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(true)
    factory
  }

  org.apache.xml.security.Init.init()

  private val credential: SAMLCredential = {
    try {
      val ks = KeyStore.getInstance("JKS")
      val input =
        if (config.hasPath("credentials.keystore.path"))
          new FileInputStream(new File(config.getString("credentials.keystore.path")))
        else
          classOf[OrelyService].getResourceAsStream(config.getString("credentials.keystore.classpath"))

      ks.load(input, config.getString("credentials.keystorePassword").toCharArray)
      SAMLCredentialFactory.createSAMLCredential(ks, config.getString("credentials.keyAlias"), config.getString("credentials.keyPassword"))
    }
    catch {
      case t: Throwable =>
        t.printStackTrace()
        null
    }
  }

  def buildSAMLSignatureRequest(hash: Array[Byte]): OrelySignRequest = {
    val xmlRequest = new XmlSignRequestGenerator().setManifest(
      new Manifest.Builder().add(
        new Reference.Builder()
          .setDigestAlgorithm(DigestAlgorithm.SHA256)
          .setDigestValue(hash)
          .setUri(ethAddressSignatureURI)
          .build()
      ).build()
    ).setSignatureForm(XmlSignatureForm.EPES).toXml
    val encodedRequest = Base64.getEncoder.encodeToString(xmlRequest.getBytes("UTF-8"))
    val params = new RequestParameters()
    params.setCertificateRequest(CertificateRequest.REQUIRED)
    params.setDssPayload(encodedRequest.getBytes("UTF-8"))
    val svc = new RequestService(
      credential,
      new URL(config.getString("luxtrust.destinationURL")),
      new URL(config.getString("luxtrust.returnURL")),
      new URL(config.getString("luxtrust.issuerURL")),
      config.getBoolean("luxtrust.ocsp")
    )
    OrelySignRequest(svc.createRequest(params).replaceAll("\\n", ""))
  }

  def parseSAMLSignatureResponse(encoded: String): OrelySignResult = {
    val svc = new ResponseService(credential, config.getBoolean("luxtrust.ocsp"))
    val response = svc.parseResponse(encoded)
    val dss = extractDSSSignature(response.getDssPayload)
    val signInfo = dss.getSignedInfo
    val manifest = extractManifest(dss)
    OrelySignResult(
      signedInfo = new String(signInfo.getCanonicalizedOctetStream, "UTF-8"),
      manifest = manifest,
      signedInfoSignature = Hex.encodeHexString(dss.getSignatureValue),
      signerCertificate = Hex.encodeHexString(dss.getKeyInfo.getX509Certificate.getEncoded)
    )
  }

  private def extractDSSSignature(payload: Array[Byte]): XMLSignature = {
    val dssXMLResponse = new String(Base64.getDecoder.decode(payload), "UTF-8")
    val dssSignResponse = new XmlSignResponseParser().parse(dssXMLResponse)
    val data = IOUtils.toByteArray(dssSignResponse.getSignatureStream)
    val builder = docBuilderFactory.newDocumentBuilder()
    val document = builder.parse(new ByteArrayInputStream(data))
    new XMLSignature(document.getDocumentElement, "")
  }

  private def extractManifest(sig: XMLSignature): String = {
    val signedInfos = sig.getSignedInfo
    val references = for {
      i <- 0 until signedInfos.getLength
    } yield signedInfos.item(i)
    references.find(r => r.getType.equals("http://www.w3.org/2000/09/xmldsig#Manifest")) match {
      case None => ""
      case Some(reference) => new String(reference.getContentsAfterTransformation.getBytes, "UTF-8")
    }
  }

}
