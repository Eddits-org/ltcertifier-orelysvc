#!/bin/bash

sbt -J--add-modules=java.xml.bind -Dconfig.file=./src/main/resources/application.dev.conf -Dlu.luxtrust.certificate.validator.config.path=./dev-config/TrustAnchors run