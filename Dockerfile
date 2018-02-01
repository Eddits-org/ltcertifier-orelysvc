FROM openjdk:9-jre-slim

EXPOSE 8080

WORKDIR /orely-svc
ENTRYPOINT java --add-modules java.xml.bind -jar -Dcredentials.keystore.path=$KEYSTORE_PATH -Dcredentials.keystorePassword=$KEYSTORE_PASSWORD -Dcredentials.keyAlias=$KEY_ALIAS -Dcredentials.keyPassword=$KEY_PASSWORD -Dlu.luxtrust.certificate.validator.config.path=$TRUST_PATH ./eth-kyc-orely-svc-assembly-0.1.jar

ADD target/scala-2.12/eth-kyc-orely-svc-assembly-0.1.jar /orely-svc

