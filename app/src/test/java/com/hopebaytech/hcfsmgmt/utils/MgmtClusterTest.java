package com.hopebaytech.hcfsmgmt.utils;

import com.hopebaytech.hcfsmgmt.httpproxy.HttpProxy;
import com.hopebaytech.hcfsmgmt.httpproxy.HttpProxyMock;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.TransferContentInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import javax.net.ssl.HttpsURLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/27.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class MgmtClusterTest {

    @Before
    public void setUp() throws Exception {
        HttpProxy.setMode(HttpProxy.MODE_MOCK);
        ShadowLog.stream = System.out;
    }

    @After
    public void tearDown() throws Exception {

    }

//    @Test
//    public void testRegisterTera() {
//        MgmtCluster.RegisterParam registerParam;
//        String correctJwtToken = HttpProxyMock.CORRECT_JWT_TOKEN;
//        String correctAuthCode = HttpProxyMock.CORRECT_AUTH_CODE;
//        String correctImei = HttpProxyMock.CORRECT_IMEI;
//        String incorrectJwtToken = HttpProxyMock.INCORRECT_JWT_TOKEN;
//        String incorrectAuthCode = HttpProxyMock.INCORRECT_AUTH_CODE;
//        String incorrectImei = HttpProxyMock.INCORRECT_IMEI;
//
//        registerParam = new MgmtCluster.RegisterParam(correctAuthCode);
//        registerParam.setAuthCode(correctAuthCode);
//        registerParam.setImei(correctImei);
//        RegisterResultInfo info = MgmtCluster.RegisterProxy.register(registerParam, correctJwtToken);
//        assertEquals(HttpsURLConnection.HTTP_OK, info.getResponseCode());
//
//        registerParam = new MgmtCluster.GoogleAuthParam();
//        registerParam.setAuthCode(incorrectAuthCode);
//        registerParam.setImei(correctImei);
//        info = MgmtCluster.RegisterProxy.register(registerParam, correctJwtToken);
//        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());
//
//        registerParam = new MgmtCluster.GoogleAuthParam();
//        registerParam.setAuthCode(correctAuthCode);
//        registerParam.setImei(incorrectImei);
//        info = MgmtCluster.RegisterProxy.register(registerParam, correctJwtToken);
//        assertEquals(HttpsURLConnection.HTTP_NOT_FOUND, info.getResponseCode());
//
//        registerParam = new MgmtCluster.GoogleAuthParam();
//        registerParam.setAuthCode(correctAuthCode);
//        registerParam.setImei(correctImei);
//        info = MgmtCluster.RegisterProxy.register(registerParam, incorrectJwtToken);
//        assertEquals(HttpsURLConnection.HTTP_FORBIDDEN, info.getResponseCode());
//    }

//    @Test
//    public void testAuth() throws Exception {
//        // Auth with correct google account
//        MgmtCluster.GoogleAuthParam googleAuthParam = new MgmtCluster.GoogleAuthParam();
//        googleAuthParam.setAuthCode(HttpProxyMock.CORRECT_AUTH_CODE);
//        AuthResultInfo info = MgmtCluster.AuthProxy.auth(googleAuthParam);
//        assertEquals(HttpsURLConnection.HTTP_OK, info.getResponseCode());
//
//        // Auth with incorrect google account
//        String incorrectAuthCode = HttpProxyMock.INCORRECT_AUTH_CODE;
//        googleAuthParam = new MgmtCluster.GoogleAuthParam();
//        googleAuthParam.setAuthCode(incorrectAuthCode);
//        info = MgmtCluster.AuthProxy.auth(googleAuthParam);
//        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());
//    }

//    @Test
//    public void testSwitchAccount() throws Exception {
//        DeviceServiceInfo deviceServiceInfo;
//        String correctJwtToken = HttpProxyMock.CORRECT_JWT_TOKEN;
//        String correctNewAuthCode = HttpProxyMock.CORRECT_NEW_AUTH_CODE;
//        String correctImei = HttpProxyMock.CORRECT_IMEI;
//        String incorrectJwtToken = HttpProxyMock.INCORRECT_JWT_TOKEN;
//        String incorrectNewAuthCode = HttpProxyMock.INCORRECT_AUTH_CODE;
//        String incorrectImei = HttpProxyMock.INCORRECT_IMEI;
//
//        deviceServiceInfo = MgmtCluster.switchAccount(correctJwtToken, correctNewAuthCode, correctImei);
//        assertTrue(deviceServiceInfo != null);
//
//        deviceServiceInfo = MgmtCluster.switchAccount(incorrectJwtToken, correctNewAuthCode, correctImei);
//        assertFalse(deviceServiceInfo != null);
//
//        deviceServiceInfo = MgmtCluster.switchAccount(correctJwtToken, incorrectNewAuthCode, correctImei);
//        assertFalse(deviceServiceInfo != null);
//
//        deviceServiceInfo = MgmtCluster.switchAccount(correctJwtToken, correctNewAuthCode, incorrectImei);
//        assertFalse(deviceServiceInfo != null);
//
//        deviceServiceInfo = MgmtCluster.switchAccount(incorrectJwtToken, incorrectNewAuthCode, correctImei);
//        assertFalse(deviceServiceInfo != null);
//
//        deviceServiceInfo = MgmtCluster.switchAccount(incorrectJwtToken, correctNewAuthCode, incorrectImei);
//        assertFalse(deviceServiceInfo != null);
//
//        deviceServiceInfo = MgmtCluster.switchAccount(correctJwtToken, incorrectNewAuthCode, incorrectImei);
//        assertFalse(deviceServiceInfo != null);
//
//        deviceServiceInfo = MgmtCluster.switchAccount(incorrectJwtToken, incorrectNewAuthCode, incorrectImei);
//        assertFalse(deviceServiceInfo != null);
//    }

    @Test
    public void testTransferDevice() throws Exception {
        TransferContentInfo transferContentInfo;
        String correctJwtToken = HttpProxyMock.CORRECT_JWT_TOKEN;
        String correctImei = HttpProxyMock.CORRECT_IMEI;
        String incorrectJwtToken = HttpProxyMock.INCORRECT_JWT_TOKEN;
        String incorrectImei = HttpProxyMock.INCORRECT_IMEI;

        transferContentInfo = MgmtCluster.TransferContentProxy.transferContents(correctJwtToken, correctImei);
        assertEquals(HttpsURLConnection.HTTP_OK, transferContentInfo.getResponseCode());

        transferContentInfo = MgmtCluster.TransferContentProxy.transferContents(incorrectJwtToken, correctImei);
        assertEquals(HttpsURLConnection.HTTP_FORBIDDEN, transferContentInfo.getResponseCode());

        transferContentInfo = MgmtCluster.TransferContentProxy.transferContents(correctJwtToken, incorrectImei);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, transferContentInfo.getResponseCode());

        transferContentInfo = MgmtCluster.TransferContentProxy.transferContents(incorrectJwtToken, incorrectImei);
        assertEquals(HttpsURLConnection.HTTP_FORBIDDEN, transferContentInfo.getResponseCode());
    }

    @Test
    public void testGetServerClientIdFromMgmtCluster() throws Exception {
        String correctClientId = HttpProxyMock.CORRECT_CLIENT_ID;
        String incorrectClientId = HttpProxyMock.INCORRECT_CLIENT_ID;

        assertEquals(correctClientId, MgmtCluster.getServerClientId());
        assertNotEquals(incorrectClientId, MgmtCluster.getServerClientId());
    }

    @Test
    public void testIsNeedToRetryAgain() throws Exception {
        MgmtCluster.resetRetryCount();
        assertEquals(true, MgmtCluster.isNeedToRetryAgain());

        MgmtCluster.plusRetryCount();
        assertEquals(true, MgmtCluster.isNeedToRetryAgain());

        MgmtCluster.plusRetryCount();
        assertEquals(true, MgmtCluster.isNeedToRetryAgain());

        MgmtCluster.plusRetryCount();
        assertEquals(false, MgmtCluster.isNeedToRetryAgain());
    }

}
