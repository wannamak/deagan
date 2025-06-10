#!/bin/sh
scp -o PreferredAuthentications=password -o PubkeyAuthentication=no bin/libdeagan.so deagan@deagan.local:/home/deagan/lib
