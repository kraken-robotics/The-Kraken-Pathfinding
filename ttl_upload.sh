#!/bin/sh
if [ "$#" -ne 2 ]; then
    echo "Usage: ./ttl_upload.sh archive.jar /dev/ttyWHATEVER"
else
    echo "Transfert..."
    echo "stty -echo" > $2
    sleep 0.1
    echo -n "echo \"" >> $2
    base64 --wrap=0 $1 >> $2
    echo "\"| base64 -d > $(date +%F_%s)_$1" >> $2
    echo -n "if [ $(sha1sum $1 | cut -d " " -f 1) != \"$" >> $2
    echo "(sha1sum $(date +%F_%s)_$1 | cut -d \" \" -f 1)\" ]; then echo "Erreur de checksum"; fi" >> $2
    echo "stty echo" >> $2
    echo "Done."
fi
