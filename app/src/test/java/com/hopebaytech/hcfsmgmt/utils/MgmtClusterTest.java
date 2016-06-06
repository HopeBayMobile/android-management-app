package com.hopebaytech.hcfsmgmt.utils;

import com.hopebaytech.hcfsmgmt.httpproxy.HttpProxy;
import com.hopebaytech.hcfsmgmt.httpproxy.HttpProxyMock;
import com.hopebaytech.hcfsmgmt.httpproxy.IHttpProxy;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.net.ssl.HttpsURLConnection;

import static org.junit.Assert.assertEquals;
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
    }

    @After
    public void tearDown() throws Exception {

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
        String oldAuthCode = HttpProxyMock.CORRECT_OLD_AUTH_CODE;
        String newAuthCode = HttpProxyMock.CORRECT_NEW_AUTH_CODE;
        String imei = HttpProxyMock.CORRECT_IMEI;
        boolean switchSuccess = MgmtCluster.switchAccount(oldAuthCode, newAuthCode, imei);
        assertTrue(switchSuccess);
    }

    @Test
    public void testGetServerClientIdFromMgmtCluster() throws Exception {
        final String expectedClientId = "795577377875-1tj6olgu34bqi7afnnmavvm5hj5vh1tr.apps.googleusercontent.com";
        assertEquals(expectedClientId, MgmtCluster.getServerClientId());
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