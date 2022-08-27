#!/bin/bash
#
#    Copyright 2015-2022 the original author or authors.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

SERVER_PORT=8080

rootDir=$(
  cd "$(dirname "$0")"/.. || exit
  pwd
)

printf "%s \033[32mINFO\033[m  : %s\n" "$(date '+%Y-%m-%d %H:%M:%S.000')" "Start run fat jars"

targets=$(find "${rootDir}" -type f | grep -E "target/mybatis-spring-boot-sample-[a-z]*-[\.1-9]*.*\.jar$")

for target in ${targets}; do
  printf "\n%s \033[32mINFO\033[m  : %s\n" "$(date '+%Y-%m-%d %H:%M:%S.000')" "Run the '$(basename "${target}")'"
  if [[ "${target}" == *web* ]]; then
    java -jar "${target}" &
    pid=$!
    for i in $(seq 10); do
      if [ "$(ps -p "${pid}" | grep -c "")" = "2" ] && [ "$(lsof -i:${SERVER_PORT})" = "" ]; then
        sleep 1
      else
        break
      fi
    done
    statusCode="$(curl -s http://localhost:${SERVER_PORT}/cities/US -o /dev/null -w '%{http_code}\n')"
    if [ "${statusCode}" = "200" ]; then resultCode=0; else resultCode=${statusCode}; fi
    kill ${pid}
    wait ${pid}
  else
    java -jar "${target}" && resultCode=0 || resultCode=$?
  fi
  results="${results}${target} ${resultCode}"$'\n'
done

exitCode=0
while read -r line; do
  if [ ! "${line}" = "" ]; then
    set ${line}
    target=${1}
    resultCode=${2}
    if [ "${resultCode}" = "0" ]; then
      printf "%s \033[32mINFO\033[m  : %s\n" "$(date '+%Y-%m-%d %H:%M:%S.000')" "Succeed an execution of '$(basename "${target}")'"
    else
      exitCode=1
      printf "%s \033[31mERROR\033[m : %s\n" "$(date '+%Y-%m-%d %H:%M:%S.000')" "Faild an execution of '$(basename "${target}")' resultCode=${resultCode}"
    fi
  fi
done <<END
${results}
END

exit ${exitCode}
