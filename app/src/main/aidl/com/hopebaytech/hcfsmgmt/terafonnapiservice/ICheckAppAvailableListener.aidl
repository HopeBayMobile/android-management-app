package com.hopebaytech.hcfsmgmt.terafonnapiservice;

interface ICheckAppAvailableListener {

    void onCheckCompleted(String packageName, int status);

}
