#!/bin/sh
set -e
ant
scp -o PreferredAuthentications=password -o PubkeyAuthentication=no deagan.jar deagan@deagan.local:/home/deagan/lib/deagan.jar
#ssh deagan.local "sudo systemctl restart deagan"
