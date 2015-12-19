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

package suonos.httpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.am0e.webc.RequestHandler;
import com.github.am0e.webc.WebApp;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

public class VertxRequestHandler implements Handler<HttpServerRequest> {

    final WebApp app;
    final RequestHandler requestHandler;
    private Vertx vertx;

    final static Logger log = LoggerFactory.getLogger(VertxRequestHandler.class);

    public VertxRequestHandler(Vertx vertx, WebApp app) {
        this.vertx = vertx;
        this.app = app;
        this.requestHandler = app.createRequestHandler();
    }

    @Override
    public void handle(HttpServerRequest event) {
        try {
            requestHandler.handleRequest(new VertxWebRequestImpl(app, vertx, event));

        } catch (Exception ex) {
            log.error("Unexpected Exception", ex);
        }
    }

}
