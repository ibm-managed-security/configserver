#! /bin/bash -e

CHECKOUT_DIR=/opt/config-repo

mkdir "$CHECKOUT_DIR" -p 

git clone "$GIT_URL" "$CHECKOUT_DIR"

( cd "$CHECKOUT_DIR" && transcrypt -c aes-256-cbc -p "$TRANSCRYPT_KEY" <<< $'\ny\n')
