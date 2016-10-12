#!/bin/bash
java -jar "$1"
read -rsp $'Press escape to continue...\n' -d $'\e'
