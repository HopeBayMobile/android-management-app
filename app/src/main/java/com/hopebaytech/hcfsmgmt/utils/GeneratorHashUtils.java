/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hopebaytech.hcfsmgmt.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by rondou.chen on 2017/9/11.
 */

public class GeneratorHashUtils {
    public static String generateSHA1(String message) {
        return hashString(message, "SHA-1");
    }

    private static String hashString(String message, String algorithm) {

        try {
            MessageDigest sha = MessageDigest.getInstance(algorithm);
            byte[] hashedBytes = sha.digest(message.getBytes("UTF-8"));

            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            return null;
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0xF00, 16).substring(1));
        }
        return stringBuffer.toString();
    }

    /**
    * @return a non-negative integer generated from 20 byte SHA-1 hash.
    */
    private static int getSmallHashFromSha1(byte[] sha1) {
        final int offset = sha1[19] & 0xf; // SHA1 is 20 bytes.
        return ((sha1[offset]  & 0x7f) << 24)
                | ((sha1[offset + 1] & 0xff) << 16)
                | ((sha1[offset + 2] & 0xff) << 8)
                | ((sha1[offset + 3] & 0xff));
    }
}


