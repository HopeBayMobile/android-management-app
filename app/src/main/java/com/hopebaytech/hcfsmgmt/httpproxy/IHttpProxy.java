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
package com.hopebaytech.hcfsmgmt.httpproxy;

import android.content.ContentValues;

import java.io.IOException;

/**
 * @author Aaron
 *         Created by Aaron on 2016/6/3.
 */
public interface IHttpProxy {

    void setUrl(String url);

    void setHeaders(ContentValues cv);

    void connect() throws IOException;

    int post(ContentValues cv) throws IOException;

    int get() throws IOException;

    String getResponseContent() throws IOException;

    void disconnect();

    void setDoOutput(boolean allowPost);

}
