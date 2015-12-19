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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;

import com.github.am0e.commons.utils.CommonDates;
import com.github.am0e.commons.utils.UrlQueryParser;
import com.github.am0e.commons.utils.Validate;
import com.github.am0e.webc.AsyncWebResult;
import com.github.am0e.webc.Handler;
import com.github.am0e.webc.HttpMethod;
import com.github.am0e.webc.HttpStatus;
import com.github.am0e.webc.WebApp;
import com.github.am0e.webc.WebRequest;
import com.github.am0e.webc.WebResponse;
import com.github.am0e.webc.impl.WebRequestImpl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class VertxWebRequestImpl extends WebRequestImpl implements VertxWebRequest, WebResponse {

    /**
     * The servlet request. Note that this field may be null if the web context
     * has been created outside of a web request. For example the newsletter
     * service will create a web context to send out emails.
     */
    private HttpServerRequest request;
    private Buffer responseData;
    private Writer writer;
    private OutputStream outputStream;
    private String characterEncoding = "UTF-8";
    private Vertx vertx;

    public VertxWebRequestImpl(WebApp webApp, Vertx vertx, HttpServerRequest request) {
        super(webApp, Locale.getDefault());
        this.request = request;
        this.vertx = vertx;
        init();
    }

    private void init() {

        this.setMethod(HttpMethod.getMethod(request.method().name()));

        // Get the request path.
        //
        String query = request.query();

        if (query != null) {
            UrlQueryParser parser = new UrlQueryParser();
            parser.parseMultiValuesAsArray();
            params.putAll(parser.parse(query));
        }
    }

    @Override
    public final String pathInfo() {
        return request.path();
    }

    @Override
    public final String path() {
        return request.path();
    }

    @Override
    public final boolean isSecure() {
        return request.absoluteURI() == null ? true : false;
    }

    @Override
    public final String remoteAddr() {
        return request.remoteAddress().toString();
    }

    @Override
    public final String getHeader(String name) {
        return request.headers().get(name);
    }

    @Override
    public String serverName() {
        return request.localAddress().host();
    }

    @Override
    public void setContentType(String contentType) {
        request.response().headers().set("Content-Type", contentType);
    }

    @Override
    public void setCharacterEncoding(String encoding) {
        this.characterEncoding = encoding;
    }

    @Override
    public void startAsync() {
        throw Validate.notImplemented();
    }

    @Override
    public void setHeader(String name, String value) {
        request.response().headers().set(name, value);
    }

    @Override
    public void setStatusCode(int statusCode) {
        request.response().setStatusCode(statusCode);
    }

    @Override
    public boolean isCommitted() {
        return request.response().headWritten();
    }

    @Override
    public String queryString() {
        return request.query();
    }

    @Override
    public String fullContextPath() {
        return "";
    }

    @Override
    public void addHeader(String name, String value) {
        request.response().headers().add(name, value);
    }

    @Override
    public String protocol() {
        // return ex.getProtocol();
        return null;
    }

    @Override
    public void sendRedirect(String url, boolean isPermanent, Object flashObject) throws IOException {
        // if (flashObject!=null)
        // attr(HttpSession.class).setAttribute("flash.object", flashObject);

        if (isPermanent) {
            setStatusCode(301);
            addHeader("Location", encodeRedirectURL(url));
            addHeader("Connection", "close");
        } else {
            setStatusCode(307);
            addHeader("Location", encodeRedirectURL(url));
        }
        commit();
    }

    private void commit() throws IOException {
        OutputStream os = getOutputStream();
        os.close();
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url;
    }

    @Override
    public void setDateHeader(String name, long value) {
        setHeader(name, CommonDates.formatRFC822(new Date(value)));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public Writer getWriter() throws IOException {
        if (writer == null) {
            writer = new OutputStreamWriter(getOutputStream(), getCharacterEncoding());
        }
        return writer;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            responseData = Buffer.buffer();

            outputStream = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    responseData.appendByte((byte) b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    responseData.appendBytes(b, off, len);
                }
            };
        }
        return outputStream;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T attr(Class<T> t) {
        return (T) attributes.get(t);
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public int getStatusCode() {
        return request.response().getStatusCode();
    }

    @Override
    public HttpServerRequest httpServerRequest() {
        return this.request;
    }

    @Override
    public HttpServerResponse httpServerResponse() {
        return this.request.response();
    }

    @Override
    public WebResponse response() {
        return this;
    }

    /**
     * Called when the request handler has finished processing. If we have data
     * to write, send it to the wire otherwise we leave the connection open.
     */
    @Override
    public void leaveRequest() {
        if (responseData != null) {
            // End with the response data.
            // It will be written async to the wire.
            //
            this.request.response().end(responseData);
            this.responseData = null;
        }
    }

    private boolean ended() {
        return this.request.response().ended();
    }

    @Override
    public void end() {
        this.request.response().end();
    }

    /**
     * Send the file. This will end the request.
     */
    @Override
    public void sendFile(Path sourceFile) {
        this.responseData = null;
        this.request.response().sendFile(sourceFile.toString());
    }

    @Override
    public void sendError(int status, String msg) throws IOException {
        this.responseData = null;
        this.setContentType("text/html");
        this.setStatusCode(status);
        this.request.response().end(msg);
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }

    @Override
    public void asyncExec(Handler<WebRequest> codeHandler, Handler<AsyncWebResult> resultHandler) {

        vertx.executeBlocking(future -> {
            try {
                codeHandler.handle(this);
                future.complete();

            } catch (Exception e) {
                future.fail(e);
            }
        },

                result -> {
                    try {
                        resultHandler.handle(new VertxAsyncWebResult(result, this));
                        if (!ended()) {
                            if (result.failed()) {
                                setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                                end();
                            }
                        }

                    } catch (Exception e) {
                        if (!ended()) {
                            setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                            end();
                        }
                        // e.printStackTrace();
                    }
                });
    }

    private final static class VertxAsyncWebResult implements AsyncWebResult {

        private AsyncResult<Object> result;
        private WebRequest request;

        public VertxAsyncWebResult(AsyncResult<Object> result, WebRequest request) {
            this.result = result;
            this.request = request;
        }

        @Override
        public Throwable cause() {
            return result.cause();
        }

        @Override
        public boolean succeeded() {
            return result.succeeded();
        }

        @Override
        public boolean failed() {
            return result.failed();
        }

        @Override
        public WebRequest request() {
            return request;
        }
    }
}
