#!/bin/sh

# on démarre le serveur vnc sur :1
vncserver -kill :1
vncserver :1 -geometry 630x463 -depth 8
