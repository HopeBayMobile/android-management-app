#!/bin/bash
#########################################################################
#
# Copyright © 2016 Hope Bay Technologies, Inc. All rights reserved.
#
# Abstract:
#
# Required Env Variable:
# Revision History
#   2016/3/7 Jethro Add ci script for android app
#
##########################################################################
[ $EUID -eq 0 ] || exec sudo -s -E $0
echo -e "\n======== ${BASH_SOURCE[0]} ========"
repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && while [ ! -d .git ] ; do cd ..; done; pwd )"
here="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

export TERM=xterm-256color
erorr_at() {
	$UNTRACE
	local script="$1"
	local parent_lineno="$2"
	local message="$3"
	local code="${4:-1}"
	echo "Error near ${script} line ${parent_lineno}; exiting with status ${code}"
	if [[ -n "$message" ]] ; then
		echo -e "Message: ${message}"
	fi
	exit "${code}"
}

#let printf handle the printing
function _hashes() { printf %0$((${1}))d\\n | tr 0 \# ; }
function _hdr_inc() {
	{ $UNTRACE; } 2>/dev/null
	local _hinc=${1##*-} _hashc=${2##*[!0-9]}
	: ${_hinc:=$(set -- $3 ; printf %s_cnt\\n "${1-_hdr}")}
	${1+shift} ${2+2}
	_hashes ${_hashc:=40}
	printf "%s #$((${_hinc}=${_hinc}+1)):${1+%b}" \
		${1+"$*"} ${1+\\c}Default
	echo && _hashes $_hashc
	$TRACE
}
function build_system() {
	{ _hdr_inc - - Doing $FUNCNAME; } 2>/dev/null
	docker pull $DOCKER_IMAGE
	echo sdk.dir=/opt/android-sdk-linux > local.properties
	echo ndk.dir=/opt/android-ndk-r10e >> local.properties
	docker run --rm \
		--volume="$(pwd):/opt/workspace" \
		--volume="$(pwd)/.root.gradle:/root/.gradle" \
		--volume="/mnt/nas/CloudDataSolution/HCFS_android/resources/ci_shared_storage/android-sdk-linux:/opt/android-sdk-linux" \
		-e KEYSTORE_PASSWORD -e KEY_ALIAS -e KEY_PASSWORD \
		$DOCKER_IMAGE /bin/bash -c "./gradlew assembleRelease"

}
function copy_lib_to_source_tree() {
	{ _hdr_inc - - Doing $FUNCNAME; } 2>/dev/null
	rsync -arcv --no-owner --no-group --no-times --no-perms \
		$LIB_DIR/acer-s58a-hcfs/system/lib64/{libHCFS_api.so,libjansson.so} app/src/main/jni/mylibs/arm64-v8a/
}
function publish_apk() {
	{ _hdr_inc - - Doing $FUNCNAME; } 2>/dev/null
	APP_NAME=terafonn_$(sed -n -e '/versionName/p' app/build.gradle | cut -d'"' -f 2)
	APP_DIR=${PUBLISH_DIR}/${JOB_NAME}
	echo APP_NAME=${APP_NAME} >> export_props.properties
	echo APP_DIR=${APP_DIR} >> export_props.properties
	umask 0022
	mkdir -p ${PUBLISH_DIR}/${JOB_NAME}
	rm -rf ${PUBLISH_DIR}/${JOB_NAME}/*
	if [ -f app/build/outputs/apk/app-release.apk ]; then
		rsync -arcv --chmod=a+rX --no-owner --no-group --no-times \
			app/build/outputs/apk/app-release.apk ${PUBLISH_DIR}/${JOB_NAME}/${APP_NAME}.apk
	fi
	if [ -f app/build/outputs/apk/app-release-unsigned.apk ]; then
		rsync -arcv --chmod=a+rX --no-owner --no-group --no-times \
			app/build/outputs/apk/app-release-unsigned.apk ${PUBLISH_DIR}/${JOB_NAME}/${APP_NAME}.apk
	fi
	rsync -arcv --chmod=a+rX --no-owner --no-group --no-times \
		app/src/main/libs/arm64-v8a/libterafonnapi.so ${PUBLISH_DIR}/${JOB_NAME}/arm64-v8a/
}
function mount_nas() {
	{ _hdr_inc - - Doing $FUNCNAME; } 2>/dev/null
	service rpcbind start || :
	mkdir -p /mnt/nas
	if ! mount  | grep 'nas:/ubuntu on /mnt/nas'; then
		umount /mnt/nas || :
		mount nas:/ubuntu /mnt/nas
	fi
}
function unmount_nas() {
	{ _hdr_inc - - Doing $FUNCNAME; } 2>/dev/null
	umount /mnt/nas
}

function update_version_num() {
	if [ -z $VERSION_NUM ]; then
		export VERSION_NUM = Manual build $(shell date +%Y%m%d-%H%M%S)
	fi
	sed -i"" -e 's#\(<string name="tera_version" translatable="false">\).*\(</string>\)#\1'$VERSION_NUM'\2#' \
		app/src/main/res/values/config.xml
}

# Enable error trace
trap 'erorr_at "${BASH_SOURCE[0]}" ${LINENO}' ERR
set -e -o errtrace -o functrace

echo ========================================
echo Jenkins pass-through variables:
echo PUBLISH_DIR: ${PUBLISH_DIR}
echo LIB_DIR: ${LIB_DIR}
echo JOB_NAME: ${JOB_NAME}
echo ========================================
echo "Environment variables (with defaults):"
TRACE="set -x"; UNTRACE="set +x"
$TRACE

DOCKERNAME=android-app-build-`date +%m%d-%H%M%S`
DOCKER_IMAGE=docker:5000/android-app-buildbox
JOB_NAME=${JOB_NAME:-HCFS-android-apk}

### Upstream hcfs lib
# LIB_DIR=${LIB_DIR:-/mnt/nas/CloudDataSolution/TeraFonn_CI_build/feature/terafonn_1.0.0025/2.0.4.0305/HCFS-android-binary}
eval '[ -n "$LIB_DIR" ]' || { echo Assign these for local build; exit 1; }

### Publish dir
# PUBLISH_DIR=${PUBLISH_DIR:-/mnt/nas/CloudDataSolution/TeraFonn_CI_build/android-dev/2.0.4.ci.test}
eval '[ -n "$PUBLISH_DIR" ]' || { echo Assign these for local build; exit 1; }

mount_nas
copy_lib_to_source_tree
update_version_num
build_system
publish_apk
