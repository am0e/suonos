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
import java.net.InetSocketAddress;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.am0e.commons.msgs.Msgs;
import com.github.am0e.jdi.annotn.Global;
import com.github.am0e.jdi.annotn.Setting;
import com.github.am0e.webc.WebApp;
import com.sun.net.httpserver.HttpServer;

// mapping:
//
// <mapping path="/music/albums/?">
//       AlbumsController 		
// </mapping>
//
// /music/albums 	  -> 	albumscontroller.defaultAction() { get all albums }
// /music/albums/1234 ->    albumscontroller.defaultAction() { get album with id. }

// Get all albums:
// GET http://server/music/albums
//
// Get albums for genre:
// GET http://server/music/albums?genre=classical
//
// Get an album:
// GET http://server/music/albums/12233
//
@Singleton
@Global
public final class AppHttpServer {
    private HttpServer server;

    @Inject
    @Setting(path = "settings.httpserver.bind.port")
    private int port = 8080;

    @Inject
    @Setting(path = "settings.httpserver.bind.host")
    private String host = "127.0.0.1";

    /**
     * Create http server instance.
     * 
     * @throws IOException
     */
    public AppHttpServer(WebApp webApp, FileServlet fileServlet) throws IOException {

        server = HttpServer.create();

        InetSocketAddress adr = new InetSocketAddress(this.host, this.port);
        ServletImpl servlet = new ServletImpl(webApp);
        fileServlet.setContextPath("/res");

        server.bind(adr, 0);
        server.createContext("/res", fileServlet);
        server.createContext("/", servlet);
        server.start();
    }

    public String getUrl() {
        return Msgs.format("http://{}:{}", host, port);
    }
}
