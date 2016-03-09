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
    docker run --tty --interactive --rm --volume=$(pwd):/opt/workspace \
        -e KEYSTORE_PASSWORD -e KEY_ALIAS -e KEY_PASSWORD \
        $DOCKER_IMAGE /bin/sh -c "./gradlew clean assembleRelease"
}
function copy_lib_to_source_tree() {
	{ _hdr_inc - - Doing $FUNCNAME; } 2>/dev/null
	rsync -arcv --no-owner --no-group --no-times \
		$UPSTREAM_LIB_DIR/acer-s58a-hcfs/system/lib64/{libHCFS_api.so,libjansson.so} app/src/main/jni/mylibs/arm64-v8a/
}
function publish_apk() {
	{ _hdr_inc - - Doing $FUNCNAME; } 2>/dev/null
	rsync -v \
		app/build/outputs/apk/app-release.apk app/src/main/libs/arm64-v8a \
		${PUBLISH_DIR}/${JOB_NAME}/
}
function mount_nas() {
	{ _hdr_inc - - Doing $FUNCNAME; } 2>/dev/null
	service rpcbind start || :
	if ! mount  | grep 'nas:/ubuntu on /mnt/nas'; then
		umount /mnt/nas || :
		mount nas:/ubuntu /mnt/nas
	fi
}
function unmount_nas() {
	{ _hdr_inc - - Doing $FUNCNAME; } 2>/dev/null
	umount /mnt/nas
}

# Enable error trace
trap 'erorr_at "${BASH_SOURCE[0]}" ${LINENO}' ERR
set -e -o errtrace -o functrace

echo ========================================
echo Jenkins pass-through variables:
echo PUBLISH_DIR: ${PUBLISH_DIR}
echo UPSTREAM_LIB_DIR: ${UPSTREAM_LIB_DIR}
echo JOB_NAME: ${JOB_NAME}
echo ========================================
echo "Environment variables (with defaults):"
TRACE="set -x"; UNTRACE="set +x"
$TRACE

APK_NAME=terafonn_1.0.0024
DOCKERNAME=s58a-image-build-`date +%m%d-%H%M%S`
DOCKER_IMAGE=docker:5000/android-app-buildbox

### Upstream hcfs lib
# UPSTREAM_LIB_DIR=${UPSTREAM_LIB_DIR:-/mnt/nas/CloudDataSolution/TeraFonn_CI_build/device/s58a_ci/2.0.3.0261/HCFS-android-binary}
eval '[ -n "$UPSTREAM_LIB_DIR" ]' || { echo Assign these for local build; exit 1; }

### Publish dir
# PUBLISH_DIR=${PUBLISH_DIR:-/mnt/nas/CloudDataSolution/TeraFonn_CI_build/android-dev/2.0.3.ci.test}
eval '[ -n "$PUBLISH_DIR" ]' || { echo Assign these for local build; exit 1; }
JOB_NAME=${JOB_NAME:-HCFS-android-apk}

copy_lib_to_source_tree
#build_system
mount_nas
publish_apk
