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
FROM ubuntu:14.04

MAINTAINER Jacek Marchwicki "jacek.marchwicki@gmail.com"

# Use local APT cache
RUN sed -i 's/archive.ubuntu.com/free.nchc.org.tw/g' /etc/apt/sources.list && echo 'Acquire::http::Proxy "http://10.0.1.5:8000";' > /etc/apt/apt.conf.d/30aptproxy && apt update

# Install java7
RUN apt-get install -y software-properties-common && add-apt-repository -y ppa:webupd8team/java && apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java8-installer

# Install Deps
RUN dpkg --add-architecture i386 && apt-get update && \
	apt-get install -y --force-yes expect git wget libc6-i386 lib32stdc++6 lib32gcc1 lib32ncurses5 lib32z1 python curl make

# Install Android SDK
RUN cd /opt && wget ftp://nas/ubuntu/CloudDataSolution/HCFS_android/resources/android-sdk_r24.4.1-linux.tgz -O - | tar zxv

# Install Android NDK
RUN cd /opt && wget ftp://nas/ubuntu/CloudDataSolution/HCFS_android/resources/android-ndk-r10e-linux-x86_64.bin && chmod u+x android-ndk-r10e-linux-x86_64.bin && ./android-ndk-r10e-linux-x86_64.bin && rm -f android-ndk-r10e-linux-x86_64.bin && ls -l

# Setup environment
ENV SDK_HOME /opt/android-sdk-linux
ENV NDK_HOME /opt/android-ndk-r10e
ENV PATH ${PATH}:${SDK_HOME}/tools:${SDK_HOME}/platform-tools:${NDK_HOME}

# Install sdk elements
RUN ( while true; do sleep 3; echo y; done ) | android update sdk --all --force --no-ui --filter android-23,build-tools-23.0.3,extra-android-m2repository,extra-android-support,extra-google-google_play_services,extra-google-m2repository,platform-tools

RUN which adb && which android && which ndk-build

# Cleaning
RUN apt-get clean

# GO to workspace
RUN mkdir -p /opt/workspace
WORKDIR /opt/workspace
VOLUME /opt/workspace
ENV GRADLE_USER_HOME /root/.gradle
COPY . /opt/workspace/
RUN ls -la /opt/workspace/ && ./gradlew assembleDebug
