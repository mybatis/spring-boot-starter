#!/bin/bash

targetMinorVersion=$1
majorVersion=${targetMinorVersion%.*}
minorVersion=${targetMinorVersion#*.}
while read -r line; do
  maintenanceVersion=${line#<a*>${targetMinorVersion}.} && maintenanceVersion=${maintenanceVersion%%.*}
  maintenanceVersions="${maintenanceVersions}${maintenanceVersion}"$'\n'
done<<END
  $(curl -s "https://repo1.maven.org/maven2/org/springframework/boot/spring-boot/" | grep "RELEASE" | grep -E ">${majorVersion}\.${minorVersion}\.[0-9]*")
END
echo "${targetMinorVersion}.$(echo "${maintenanceVersions}" | sort -n | tail -n 1).RELEASE"
