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

import java.io.IOException;

import javax.inject.Inject;

import com.github.am0e.commons.msgs.Msgs;
import com.github.am0e.jdi.BeanContainer;
import com.github.am0e.jdi.annotn.Setting;
import com.github.am0e.jdi.interfaces.Startable;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

public class VertxHttpServer implements Startable, Runnable {
    private VertxWebApp webApp;

    @Inject
    @Setting(path = "settings.httpserver.bind.port")
    private int port = 8080;

    @Inject
    @Setting(path = "settings.httpserver.bind.host")
    private String host = "127.0.0.1";

    private Thread thread;

    private HttpServer server;

    private VertxStaticFileHandler staticFileHandler;

    private Vertx vertx;

    /**
     * Create http server instance.
     * 
     * @throws IOException
     */
    public VertxHttpServer(Vertx vertx, VertxWebApp webApp, VertxStaticFileHandler staticFileHandler)
            throws IOException {
        this.webApp = webApp;
        this.vertx = vertx;
        this.staticFileHandler = staticFileHandler;
    }

    public String getUrl() {
        return Msgs.format("http://{}:{}", host, port);
    }

    @Override
    public void start(BeanContainer container) throws Exception {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void stop(BeanContainer container) throws Exception {
        server.close();
    }

    @Override
    public void run() {
        HttpServerOptions opts = new HttpServerOptions();
        opts.setHost(host);
        opts.setPort(port);
        opts.setCompressionSupported(true);

        staticFileHandler.setContextPath("/res");
        server = vertx.createHttpServer(opts);

        webApp.routes().addRouteHandler("/res/:path*", staticFileHandler);
        server.requestHandler(new VertxRequestHandler(vertx, webApp));
        server.listen();
    }
}
