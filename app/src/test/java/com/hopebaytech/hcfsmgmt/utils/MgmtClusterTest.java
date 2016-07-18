package com.hopebaytech.hcfsmgmt.utils;

import com.hopebaytech.hcfsmgmt.httpproxy.HttpProxy;
import com.hopebaytech.hcfsmgmt.httpproxy.HttpProxyMock;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.RegisterResultInfo;
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

    @Test
    public void testRegisterTera() {
        MgmtCluster.GoogleAuthParam googleAuthParam;
        MgmtCluster.UserAuthParam userAuthParam;
        String correctJwtToken = HttpProxyMock.CORRECT_JWT_TOKEN;
        String correctAuthCode = HttpProxyMock.CORRECT_AUTH_CODE;
        String correctImei = HttpProxyMock.CORRECT_IMEI;
        String correctUsername = HttpProxyMock.CORRECT_USER_NAME;
        String correctPassword = HttpProxyMock.CORRECT_USER_PASSWORD;
        String correctActivationCode = HttpProxyMock.CORRECT_ACTIVATION_CODE;
        String incorrectJwtToken = HttpProxyMock.INCORRECT_JWT_TOKEN;
        String incorrectAuthCode = HttpProxyMock.INCORRECT_AUTH_CODE;
        String incorrectImei = HttpProxyMock.INCORRECT_IMEI;
        String incorrectUsername = HttpProxyMock.INCORRECT_USER_NAME;
        String incorrectPassword = HttpProxyMock.INCORRECT_USER_PASSWORD;
        String incorrectActivationCode = HttpProxyMock.INCORRECT_ACTIVATION_CODE;

        googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(correctAuthCode);
        googleAuthParam.setImei(correctImei);
        RegisterResultInfo info = MgmtCluster.RegisterProxy.register(googleAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_OK, info.getResponseCode());

        googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(incorrectAuthCode);
        googleAuthParam.setImei(correctImei);
        info = MgmtCluster.RegisterProxy.register(googleAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(correctAuthCode);
        googleAuthParam.setImei(incorrectImei);
        info = MgmtCluster.RegisterProxy.register(googleAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_NOT_FOUND, info.getResponseCode());

        googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(correctAuthCode);
        googleAuthParam.setImei(correctImei);
        info = MgmtCluster.RegisterProxy.register(googleAuthParam, incorrectJwtToken);
        assertEquals(HttpsURLConnection.HTTP_FORBIDDEN, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(correctUsername);
        userAuthParam.setPassword(correctPassword);
        userAuthParam.setActivateCode(correctActivationCode);
        userAuthParam.setImei(correctImei);
        info = MgmtCluster.RegisterProxy.register(userAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_OK, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(incorrectUsername);
        userAuthParam.setPassword(correctPassword);
        userAuthParam.setActivateCode(correctActivationCode);
        userAuthParam.setImei(correctImei);
        info = MgmtCluster.RegisterProxy.register(userAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(correctUsername);
        userAuthParam.setPassword(incorrectPassword);
        userAuthParam.setActivateCode(correctActivationCode);
        userAuthParam.setImei(correctImei);
        info = MgmtCluster.RegisterProxy.register(userAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(correctUsername);
        userAuthParam.setPassword(correctPassword);
        userAuthParam.setActivateCode(incorrectActivationCode);
        userAuthParam.setImei(correctImei);
        info = MgmtCluster.RegisterProxy.register(userAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(correctUsername);
        userAuthParam.setPassword(correctPassword);
        userAuthParam.setActivateCode(correctActivationCode);
        userAuthParam.setImei(incorrectImei);
        info = MgmtCluster.RegisterProxy.register(userAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_NOT_FOUND, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(correctUsername);
        userAuthParam.setPassword(correctPassword);
        userAuthParam.setActivateCode(correctActivationCode);
        userAuthParam.setImei(correctImei);
        info = MgmtCluster.RegisterProxy.register(userAuthParam, incorrectJwtToken);
        assertEquals(HttpsURLConnection.HTTP_FORBIDDEN, info.getResponseCode());
    }

    @Test
    public void testAuth() throws Exception {
        // Auth with correct google account
        MgmtCluster.GoogleAuthParam googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(HttpProxyMock.CORRECT_AUTH_CODE);
        AuthResultInfo info = MgmtCluster.AuthProxy.auth(googleAuthParam);
        assertEquals(HttpsURLConnection.HTTP_OK, info.getResponseCode());

        // Auth with incorrect google account
        String incorrectAuthCode = HttpProxyMock.INCORRECT_AUTH_CODE;
        googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(incorrectAuthCode);
        info = MgmtCluster.AuthProxy.auth(googleAuthParam);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        // Auth with correct user/password
        MgmtCluster.UserAuthParam userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(HttpProxyMock.CORRECT_USER_NAME);
        userAuthParam.setPassword(HttpProxyMock.CORRECT_USER_PASSWORD);
        info = MgmtCluster.AuthProxy.auth(userAuthParam);
        assertEquals(HttpsURLConnection.HTTP_OK, info.getResponseCode());

        // Auth with incorrect user/password
        String incorrectUserName = HttpProxyMock.INCORRECT_USER_NAME;
        String incorrectUserPassword = HttpProxyMock.INCORRECT_USER_PASSWORD;
        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(incorrectUserName);
        userAuthParam.setPassword(incorrectUserPassword);
        info = MgmtCluster.AuthProxy.auth(userAuthParam);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());
    }

    @Test
    public void testSwitchAccount() throws Exception {
        RegisterResultInfo registerResultInfo;
        String correctJwtToken = HttpProxyMock.CORRECT_JWT_TOKEN;
        String correctNewAuthCode = HttpProxyMock.CORRECT_NEW_AUTH_CODE;
        String correctImei = HttpProxyMock.CORRECT_IMEI;
        String incorrectJwtToken = HttpProxyMock.INCORRECT_JWT_TOKEN;
        String incorrectNewAuthCode = HttpProxyMock.INCORRECT_AUTH_CODE;
        String incorrectImei = HttpProxyMock.INCORRECT_IMEI;

        registerResultInfo = MgmtCluster.switchAccount(correctJwtToken, correctNewAuthCode, correctImei);
        assertTrue(registerResultInfo != null);

        registerResultInfo = MgmtCluster.switchAccount(incorrectJwtToken, correctNewAuthCode, correctImei);
        assertFalse(registerResultInfo != null);

        registerResultInfo = MgmtCluster.switchAccount(correctJwtToken, incorrectNewAuthCode, correctImei);
        assertFalse(registerResultInfo != null);

        registerResultInfo = MgmtCluster.switchAccount(correctJwtToken, correctNewAuthCode, incorrectImei);
        assertFalse(registerResultInfo != null);

        registerResultInfo = MgmtCluster.switchAccount(incorrectJwtToken, incorrectNewAuthCode, correctImei);
        assertFalse(registerResultInfo != null);

        registerResultInfo = MgmtCluster.switchAccount(incorrectJwtToken, correctNewAuthCode, incorrectImei);
        assertFalse(registerResultInfo != null);

        registerResultInfo = MgmtCluster.switchAccount(correctJwtToken, incorrectNewAuthCode, incorrectImei);
        assertFalse(registerResultInfo != null);

        registerResultInfo = MgmtCluster.switchAccount(incorrectJwtToken, incorrectNewAuthCode, incorrectImei);
        assertFalse(registerResultInfo != null);
    }

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
