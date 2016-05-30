package com.hopebaytech.hcfsmgmt.utils;

import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/27.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class MgmtClusterTest {

    private final String mExpectedClientId = "795577377875-1tj6olgu34bqi7afnnmavvm5hj5vh1tr.apps.googleusercontent.com";

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSwitchAccount() throws Exception {

    }

    @Test
    public void testGetServerClientIdFromMgmtCluster() throws Exception {
        assertEquals(mExpectedClientId, MgmtCluster.getServerClientIdFromMgmtCluster());
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