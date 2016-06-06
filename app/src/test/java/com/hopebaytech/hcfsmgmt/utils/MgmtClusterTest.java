package com.hopebaytech.hcfsmgmt.utils;

import android.os.Build;

import com.hopebaytech.hcfsmgmt.fragment.ActivateWoCodeFragment;
import com.hopebaytech.hcfsmgmt.httpproxy.HttpProxy;
import com.hopebaytech.hcfsmgmt.httpproxy.HttpProxyMock;
import com.hopebaytech.hcfsmgmt.httpproxy.IHttpProxy;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.RegisterResultInfo;

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
    public void testRegister() {
        MgmtCluster.GoogleAuthParam googleAuthParam;
        MgmtCluster.UserAuthParam userAuthParam;
        String correctJwtToken = HttpProxyMock.CORRECT_JWT_TOKEN;
        String correctAuthCode = HttpProxyMock.CORRECT_AUTH_CODE;
        String correctImei = HttpProxyMock.CORRECT_IMEI;
        String correctUsername = HttpProxyMock.CORRECT_USER_NAME;
        String correctPassword = HttpProxyMock.CORRECT_USER_PASSWORD;
        String correctActivationCode = HttpProxyMock.CORRECT_ACTIVATION_CODE;
        String incorrectJwtToken = "xxxxxxxxxx";
        String incorrectAuthCode = "xxxxxxxxxx";
        String incorrectImei = "xxxxxxxxxx";
        String incorrectUsername = "xxxxxxxxxx";
        String incorrectPassword = "xxxxxxxxxx";
        String incorrectActivationCode = "xxxxxxxxxx";

        googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(correctAuthCode);
        googleAuthParam.setImei(correctImei);
        RegisterResultInfo info = MgmtCluster.register(googleAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_OK, info.getResponseCode());

        googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(incorrectAuthCode);
        googleAuthParam.setImei(correctImei);
        info = MgmtCluster.register(googleAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(correctAuthCode);
        googleAuthParam.setImei(incorrectImei);
        info = MgmtCluster.register(googleAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_NOT_FOUND, info.getResponseCode());

        googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(correctAuthCode);
        googleAuthParam.setImei(correctImei);
        info = MgmtCluster.register(googleAuthParam, incorrectJwtToken);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(correctUsername);
        userAuthParam.setPassword(correctPassword);
        userAuthParam.setActivateCode(correctActivationCode);
        userAuthParam.setImei(correctImei);
        info = MgmtCluster.register(userAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_OK, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(incorrectUsername);
        userAuthParam.setPassword(correctPassword);
        userAuthParam.setActivateCode(correctActivationCode);
        userAuthParam.setImei(correctImei);
        info = MgmtCluster.register(userAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(correctUsername);
        userAuthParam.setPassword(incorrectPassword);
        userAuthParam.setActivateCode(correctActivationCode);
        userAuthParam.setImei(correctImei);
        info = MgmtCluster.register(userAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(correctUsername);
        userAuthParam.setPassword(correctPassword);
        userAuthParam.setActivateCode(incorrectActivationCode);
        userAuthParam.setImei(correctImei);
        info = MgmtCluster.register(userAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(correctUsername);
        userAuthParam.setPassword(correctPassword);
        userAuthParam.setActivateCode(correctActivationCode);
        userAuthParam.setImei(incorrectImei);
        info = MgmtCluster.register(userAuthParam, correctJwtToken);
        assertEquals(HttpsURLConnection.HTTP_NOT_FOUND, info.getResponseCode());

        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(correctUsername);
        userAuthParam.setPassword(correctPassword);
        userAuthParam.setActivateCode(correctActivationCode);
        userAuthParam.setImei(correctImei);
        info = MgmtCluster.register(userAuthParam, incorrectJwtToken);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());
    }

    @Test
    public void testAuth() throws Exception {
        // Auth with correct google account
        MgmtCluster.GoogleAuthParam googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(HttpProxyMock.CORRECT_AUTH_CODE);
        AuthResultInfo info = MgmtCluster.auth(googleAuthParam);
        assertEquals(HttpsURLConnection.HTTP_OK, info.getResponseCode());

        // Auth with incorrect google account
        String incorrectAuthCode = "xxxxxxxxxx";
        googleAuthParam = new MgmtCluster.GoogleAuthParam();
        googleAuthParam.setAuthCode(incorrectAuthCode);
        info = MgmtCluster.auth(googleAuthParam);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());

        // Auth with correct user/password
        MgmtCluster.UserAuthParam userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(HttpProxyMock.CORRECT_USER_NAME);
        userAuthParam.setPassword(HttpProxyMock.CORRECT_USER_PASSWORD);
        info = MgmtCluster.auth(userAuthParam);
        assertEquals(HttpsURLConnection.HTTP_OK, info.getResponseCode());

        // Auth with incorrect user/password
        String incorrectUserName = "bbron";
        String incorrectUserPassword = "1111";
        userAuthParam = new MgmtCluster.UserAuthParam();
        userAuthParam.setUsername(incorrectUserName);
        userAuthParam.setPassword(incorrectUserPassword);
        info = MgmtCluster.auth(userAuthParam);
        assertEquals(HttpsURLConnection.HTTP_BAD_REQUEST, info.getResponseCode());
    }

    @Test
    public void testSwitchAccount() throws Exception {
        boolean switchSuccess;
        String correctJwtToken = HttpProxyMock.CORRECT_JWT_TOKEN;
        String correctNewAuthCode = HttpProxyMock.CORRECT_NEW_AUTH_CODE;
        String correctImei = HttpProxyMock.CORRECT_IMEI;
        String incorrectJwtToken = "xxxxxxxxxx";
        String incorrectNewAuthCode = "xxxxxxxxxx";
        String incorrectImei = "xxxxxxxxxx";

        switchSuccess = MgmtCluster.switchAccount(correctJwtToken, correctNewAuthCode, correctImei);
        assertTrue(switchSuccess);

        switchSuccess = MgmtCluster.switchAccount(incorrectJwtToken, correctNewAuthCode, correctImei);
        assertFalse(switchSuccess);

        switchSuccess = MgmtCluster.switchAccount(correctJwtToken, incorrectNewAuthCode, correctImei);
        assertFalse(switchSuccess);

        switchSuccess = MgmtCluster.switchAccount(correctJwtToken, correctNewAuthCode, incorrectImei);
        assertFalse(switchSuccess);

        switchSuccess = MgmtCluster.switchAccount(incorrectJwtToken, incorrectNewAuthCode, correctImei);
        assertFalse(switchSuccess);

        switchSuccess = MgmtCluster.switchAccount(incorrectJwtToken, correctNewAuthCode, incorrectImei);
        assertFalse(switchSuccess);

        switchSuccess = MgmtCluster.switchAccount(correctJwtToken, incorrectNewAuthCode, incorrectImei);
        assertFalse(switchSuccess);

        switchSuccess = MgmtCluster.switchAccount(incorrectJwtToken, incorrectNewAuthCode, incorrectImei);
        assertFalse(switchSuccess);
    }

    @Test
    public void testGetServerClientIdFromMgmtCluster() throws Exception {
        String correctClientId = HttpProxyMock.CORRECT_CLIENT_ID;
        String incorrectClientId = "xxxxxxxxxx";

        assertEquals(correctClientId, MgmtCluster.getServerClientId());
        assertNotEquals(incorrectClientId, MgmtCluster.getServerClientId());
    }

    @Test
    public void testAuthWithMgmtCluster() throws Exception {

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
