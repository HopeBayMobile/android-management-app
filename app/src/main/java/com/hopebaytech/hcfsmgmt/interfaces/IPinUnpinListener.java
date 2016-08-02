package com.hopebaytech.hcfsmgmt.interfaces;

import com.hopebaytech.hcfsmgmt.info.ItemInfo;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/5.
 */
public interface IPinUnpinListener {

    void onPinUnpinSuccessful(ItemInfo itemInfo);

    void onPinUnpinFailed(ItemInfo itemInfo);

}
