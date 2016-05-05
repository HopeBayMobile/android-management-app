#!/bin/bash
# Header
set -v -e
ci_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $ci_dir/..
cp -f local.properties /tmp/local.properties || :
sudo git clean -dXf

# Body
echo sdk.dir=/opt/android-sdk-linux > local.properties
echo ndk.dir=/opt/android-ndk-r10e >> local.properties
docker build -t docker:5000/android-app-buildbox -f ci/Dockerfile .
docker push docker:5000/android-app-buildbox

# Footer
cp -f /tmp/local.properties local.properties || :
