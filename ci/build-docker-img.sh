#!/bin/bash
##
## Copyright (c) 2021 HopeBayTech.
##
## This file is part of Tera.
## See https://github.com/HopeBayMobile for further info.
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##     http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##

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
