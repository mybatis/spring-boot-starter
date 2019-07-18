#!/bin/bash
#
#    Copyright 2015-2019 the original author or authors.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#


# Get Commit Message
commit_message=$(git log --format=%B -n 1)
echo "Current commit detected: ${commit_message}"

# We build for several JDKs on Travis.
# Some actions, like analyzing the code (Coveralls) and uploading
# artifacts on a Maven repository, should only be made for one version.
 
# If the version is 1.8, then perform the following actions.
# 1. Upload artifacts to Sonatype.
# 2. Use -q option to only display Maven errors and warnings.
# 3. Use --settings to force the usage of our "settings.xml" file.
# 4. Notify Coveralls.
# 5. Deploy site

if [ $TRAVIS_REPO_SLUG == "mybatis/spring-boot-starter" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "1.3.x" ] && [[ "$commit_message" != *"[maven-release-plugin]"* ]]; then

  if [ $TRAVIS_JDK_VERSION == "openjdk8" ]; then

    # Deploy to sonatype
    ./mvnw clean deploy -q -Dmaven.test.redirectTestOutputToFile=true --settings ./travis/settings.xml
    echo -e "Successfully deployed SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"

    # Deploy to coveralls
    ./mvnw clean test jacoco:report coveralls:report -q -Dmaven.test.redirectTestOutputToFile=true --settings ./travis/settings.xml
    echo -e "Successfully ran coveralls under Travis job ${TRAVIS_JOB_NUMBER}"

    # Deploy to site
	# Cannot currently run site this way
	# ./mvnw site site:deploy -q --settings ./travis/settings.xml
	# echo -e "Successfully deploy site under Travis job ${TRAVIS_JOB_NUMBER}"
  fi
else
  echo "Travis build skipped"
fi
