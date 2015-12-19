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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;

import com.github.am0e.commons.utils.CommonDates;
import com.github.am0e.commons.utils.MultiValueMap;
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
import com.sun.net.httpserver.HttpExchange;

public class ServletCtx extends WebRequestImpl implements WebResponse {

    /**
     * The servlet request. Note that this field may be null if the web context
     * has been created outside of a web request. For example the newsletter
     * service will create a web context to send out emails.
     */
    private HttpExchange ex;

    /**
     * The servlet path.
     */
    private String pathInfo;

    /**
     * Status code.
     */
    private int statusCode = HttpStatus.SC_OK;

    /**
     * 
     */
    private OutputStream outputStream;
    private Writer writer;

    private String characterEncoding = "UTF-8";

    private String contentType = "text/html";

    public ServletCtx(Locale locale) {
        super(locale);
        init();
    }

    public ServletCtx(WebApp webApp, HttpExchange ex) {
        super(webApp, Locale.getDefault());
        this.ex = ex;
        init();
    }

    private void init() {

        this.setMethod(HttpMethod.getMethod(ex.getRequestMethod()));

        // Get the request path.
        //
        URI uri = ex.getRequestURI();
        String query = uri.getRawQuery();

        pathInfo = uri.getPath();

        if (query != null) {
            UrlQueryParser parser = new UrlQueryParser();
            parser.parseMultiValuesAsArray();

            super.params.putAll(new MultiValueMap(parser.parse(query)));
        }
    }

    @Override
    public final WebResponse response() {
        return this;
    }

    @Override
    public final String pathInfo() {
        return this.pathInfo;
    }

    @Override
    public final String path() {
        return ex.getRequestURI().getPath();
    }

    @Override
    public final boolean isSecure() {
        return ex.getProtocol().equalsIgnoreCase("https");
    }

    @Override
    public final String remoteAddr() {
        return ex.getRemoteAddress().toString();
    }

    @Override
    public final String getHeader(String name) {
        return ex.getRequestHeaders().getFirst(name);
    }

    @Override
    public String serverName() {
        return ex.getRemoteAddress().getHostName();
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
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
        ex.getResponseHeaders().set(name, value);
    }

    @Override
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public boolean isCommitted() {
        return outputStream != null;
    }

    @Override
    public String queryString() {
        throw new IllegalArgumentException("TODO");
    }

    @Override
    public String fullContextPath() {
        return ex.getRequestURI().toString();
    }

    @Override
    public void addHeader(String name, String value) {
        ex.getResponseHeaders().add(name, value);
    }

    @Override
    public String protocol() {
        return ex.getProtocol();
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
    public void sendError(int status, String msg) throws IOException {
        throw Validate.notImplemented();
    }

    @Override
    public void setDateHeader(String name, long value) {
        setHeader(name, CommonDates.formatRFC822(new Date(value)));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return ex.getRequestBody();
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
            // Use chunked response.
            //
            StringBuilder sb = new StringBuilder();
            sb.append(contentType);
            // sb.append("; ");
            // sb.append("charset=");
            // sb.append(characterEncoding.toUpperCase());
            ex.getResponseHeaders().set("Content-Type", sb.toString());
            ex.sendResponseHeaders(statusCode, 0);
            this.outputStream = ex.getResponseBody();
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
    public void end() {
        try {
            if (writer != null) {
                writer.flush();
            }

            if (outputStream != null) {
                outputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public void leaveRequest() {
        end();
    }

    @Override
    public void sendFile(Path sourceFile) throws IOException {
        OutputStream outputStream = getOutputStream();
        Files.copy(sourceFile, outputStream);
        outputStream.flush();
    }

    @Override
    public void asyncExec(Handler<WebRequest> codeHandler, Handler<AsyncWebResult> resultHandler) {
        throw Validate.notImplemented();
    }
}
