#!/bin/sh

# installing certificates
KEYSTORE_PASS="changeit"
CERTS_DIR="/var/opt/rightsprotect/cer"

for cerfile in "$CERTS_DIR"/*.cer
do
  if [ -f "$cerfile" ]
  then
    filename=$(basename "$cerfile")
    filename="${filename%.*}"
    echo "Checking cacerts to add $cerfile with alias $filename"

    keytool -cacerts -storepass "$KEYSTORE_PASS" -list -alias "$filename" > /dev/null 2>&1
    if [ $? -eq 0 ]
    then
      echo "Alias of $filename already found, skipping insertion into cacerts"
    else
      echo "Inserting into cacerts under alias of $filename"
      keytool -noprompt -cacerts -storepass "$KEYSTORE_PASS" -import -trustcacerts -alias "$filename" -file "$cerfile"
    fi
  fi
done

echo $TZ > /etc/timezone
java -cp nextlabs-bulkprotect.jar:xlib/*:conf -Xmx2048m com.nextlabs.bulkprotect.App
