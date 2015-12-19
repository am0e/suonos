/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package suonos.httpserverex;

import java.io.IOException;

import com.github.am0e.webc.RequestHandler;
import com.github.am0e.webc.WebApp;
import com.github.am0e.webc.WebRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ServletImpl implements HttpHandler {

    protected RequestHandler requestHandler;
    protected WebApp app;

    public ServletImpl(WebApp app) {
        this.app = app;
        this.requestHandler = app.requestHandler();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            WebRequest ctx = new ServletCtx(app, exchange);
            requestHandler.handleRequest(ctx);

        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException(ex);
            }
        }
    }
}
