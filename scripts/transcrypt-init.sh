#! /bin/bash -e

CHECKOUT_DIR=/opt/config-repo

mkdir "$CHECKOUT_DIR" -p 

git clone "$GIT_URL" "$CHECKOUT_DIR"

( cd "$CHECKOUT_DIR" && transcrypt -c aes-256-cbc -p "$TRANSCRYPT_KEY" <<< $'\ny\n')

(
  mkdir /opt/ssl &&
  cd /opt/ssl && 
  /opt/jdk/bin/keytool -genkey \
    -alias configserver -storetype PKCS12 -keyalg RSA -keysize 2048 \
    -keystore keystore.p12 \
    -validity 3650 <<< $'abc123\nabc123\nTiago Lopo\nConfigServer\nConfigServer\n\n\nPL\nyes\n'
)
