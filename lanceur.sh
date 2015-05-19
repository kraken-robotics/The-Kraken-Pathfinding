#!/bin/sh
if [ "$#" -ne 1 ]; then
    echo "Usage: ./lanceur.sh archive.jar"
else
    sudo nice -n -16 java -jar -Xmx1G -Xms1G -XX:ThreadPriorityPolicy=1 $1
fi
