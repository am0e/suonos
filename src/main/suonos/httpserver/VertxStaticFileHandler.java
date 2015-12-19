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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import com.github.am0e.jdi.annotn.Setting;
import com.github.am0e.webc.HttpMethod;
import com.github.am0e.webc.HttpStatus;
import com.github.am0e.webc.WebResponse;
import com.github.am0e.webc.routes.RouteHandler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import suonos.services.MimeTypes;

/**
 * This servlet returns a static file.
 *
 * @author Robert Egglestone
 */
public class VertxStaticFileHandler implements RouteHandler<VertxRouteCtx> {

    public final static String CACHE_CONTROL_HEADER = "Cache-Control";
    public final static String EXPIRES_HEADER = "Expires";
    public final static String DATE_HEADER = "Date";

    @Inject
    @Setting(path = "settings.httpserver.files.root")
    private Path root;

    @Inject
    @Setting(path = "settings.httpserver.files.maxAge")
    private int maxAge = -1;

    private String contextPath = "/";
    private MimeTypes mimeTypes;
    private Vertx vertx;

    /**
     * Initialize the servlet, and determine the webapp root.
     */
    public VertxStaticFileHandler(Vertx vertx, MimeTypes mimeTypes) throws IOException {
        this.mimeTypes = mimeTypes;
        this.vertx = vertx;
    }

    /**
     * Look for a file matching the request.
     */
    protected Path getFile(VertxWebRequest request) {
        // find the location of the file
        //
        String relativePath = request.path();

        if (contextPath.length() != 1) {
            if (relativePath.startsWith(contextPath)) {
                relativePath = relativePath.substring(contextPath.length());
            } else {
                return null;
            }
        }

        // normalize the path
        //
        relativePath = relativePath.replace("//", "/");
        relativePath = relativePath.replace("\\\\", "/");

        // determine the file path to check"Content-Type", contentType for
        //
        String filePath = root.toAbsolutePath().toString().concat(relativePath);
        Path path = getFile(filePath);

        return path.normalize();
    }

    /**
     * Look for a file matching the specified path. This should also check
     * default extensions, and for index files in the case of a directory.
     */
    protected Path getFile(String filePath) {
        return Paths.get(filePath);
    }

    private String formatDateForHeader(Date date) {
        return DateUtils.formatDate(date, DateUtils.RFC1123);
    }

    public void serveFile(Path file, VertxWebRequest request, AsyncResult<Boolean> res) {
        int code = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        WebResponse response = request.response();

        try {
            // If succeeded and file exists:
            //
            if (res.succeeded()) {
                code = HttpStatus.SC_NOT_FOUND;

                // File exists?
                //
                if (res.result()) {
                    // Set headers.
                    //
                    setHeaders(response, file);

                    if (request.method() == HttpMethod.GET || request.method() == HttpMethod.POST) {
                        // transfer the content (async)
                        //
                        response.setStatusCode(HttpStatus.SC_OK);
                        setHeaders(response, file);
                        sendAsyncFile(file, request);
                        return;
                    }

                    if (request.method() == HttpMethod.HEAD) {
                        // head requests don't send the body
                        //
                        setHeaders(response, file);
                        code = HttpStatus.SC_OK;

                    } else {
                        code = HttpStatus.SC_METHOD_NOT_ALLOWED;
                    }
                }
            }

        } catch (Exception ex) {
            code = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }

        response.setStatusCode(code);
        response.end();
    }

    @Override
    public void handle(VertxRouteCtx ctx) throws Exception {

        VertxWebRequest request = ctx.request();

        try {
            // check the file and open it
            //
            Path file = getFile(request);

            if (file == null) {
                request.response().setStatusCode(HttpStatus.SC_NOT_FOUND);
                request.response().end();
            } else {
                vertx.fileSystem().exists(file.toString(), res -> {
                    serveFile(file, request, res);
                });
            }

        } catch (Exception ex) {
            request.response().setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            request.response().end();
        }
    }

    private void setHeaders(WebResponse response, Path file) {
        // System.out.println(fileLocation);

        // check for modifications
        //
        // long ifModifiedSince = exc.getDateHeader("If-Modified-Since");
        // BasicFileAttributes attrs = Files.readAttributes(fileLocation,
        // BasicFileAttributes.class);
        // long lastModTime = attrs.lastModifiedTime().toMillis();
        // if (lastModTime > 0) {
        // response.setDateHeader("Last-Modified", lastModTime);
        // if (ifModifiedSince != -1 && lastModTime <= ifModifiedSince) {
        // throw new NotModifiedException();
        // }
        // }

        // set cache headers
        //
        if (maxAge != -1) {
            Calendar now = Calendar.getInstance();
            response.setHeader(CACHE_CONTROL_HEADER, "max-age=" + maxAge);
            response.setHeader(DATE_HEADER, formatDateForHeader(now.getTime()));
            now.add(Calendar.SECOND, maxAge);
            response.setHeader(EXPIRES_HEADER, formatDateForHeader(now.getTime()));
        }

        // set the content type
        //
        String contentType = getContentType(file.toString());
        response.setHeader("Content-Type", contentType);
    }

    /**
     * Send the file.
     */
    private void sendAsyncFile(Path file, VertxWebRequest request) {
        request.httpServerResponse().sendFile(file.toString());
    }

    /**
     * Return the content-type the would be returned for this file name.
     */
    public String getContentType(String fileName) {
        return mimeTypes.getContentType(fileName);
    }

    public void setContextPath(String path) {
        this.contextPath = path;
    }

    public String getContextPath() {
        return this.contextPath;
    }
}
