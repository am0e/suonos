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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import com.github.am0e.jdi.annotn.Setting;
import com.github.am0e.webc.HttpStatus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import suonos.httpserver.DateUtils;
import suonos.services.MimeTypes;

/**
 * This servlet returns a static file.
 *
 * @author Robert Egglestone
 */
@SuppressWarnings("serial")
public class FileServlet implements HttpHandler {

    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";

    public final static String CACHE_CONTROL_HEADER = "Cache-Control";
    public final static String EXPIRES_HEADER = "Expires";
    public final static String DATE_HEADER = "Date";

    private int bufferSize = 4096;

    @Inject
    @Setting(path = "settings.httpserver.files.root")
    private Path root;

    @Inject
    @Setting(path = "settings.httpserver.files.maxAge")
    private int maxAge = -1;

    private String contextPath = "/";
    private MimeTypes mimeTypes;

    /**
     * Initialize the servlet, and determine the webapp root.
     */
    public FileServlet(MimeTypes mimeTypes) throws IOException {
        this.mimeTypes = mimeTypes;
    }

    /**
     * Look for a file matching the request.
     */
    protected Path getFile(HttpExchange request) {
        // find the location of the file
        //
        String relativePath = request.getRequestURI().getPath();

        if (contextPath.length() != 1) {
            relativePath = relativePath.substring(contextPath.length());
        }

        // normalize the path
        //
        relativePath = relativePath.replace("//", "/");
        relativePath = relativePath.replace("\\\\", "/");

        // determine the file path to check"Content-Type", contentType for
        //
        String filePath = root.toAbsolutePath().toString().concat(relativePath);

        Path file = getFile(filePath);

        if (Files.exists(file)) {
            return file;
        } else {
            return null;
        }
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

    /**
     * Transfer the file.
     */
    @Override
    public void handle(HttpExchange exc) throws IOException {
        int rcode = 0;

        try {
            // check the file and open it
            //
            Path fileLocation = getFile(exc);

            // System.out.println(fileLocation);

            if (fileLocation == null) {
                // file not found
                //
                exc.sendResponseHeaders(HttpStatus.SC_NOT_FOUND, 0);
                return;
            }

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
                exc.getResponseHeaders().set(CACHE_CONTROL_HEADER, "max-age=" + maxAge);
                Calendar now = Calendar.getInstance();
                exc.getResponseHeaders().set(DATE_HEADER, formatDateForHeader(now.getTime()));
                now.add(Calendar.SECOND, maxAge);
                exc.getResponseHeaders().set(EXPIRES_HEADER, formatDateForHeader(now.getTime()));
            }

            // set the content type
            //
            String contentType = guessContentTypeFromName(fileLocation.toString());
            exc.getResponseHeaders().set("Content-Type", contentType);

            if (exc.getRequestMethod().equals(METHOD_HEAD)) {
                // head requests don't send the body
                //
            } else if (exc.getRequestMethod().equals(METHOD_GET) || exc.getRequestMethod().equals(METHOD_POST)) {
                // transfer the content
                //
                sendFile(fileLocation, exc);

            } else {
                rcode = HttpStatus.SC_METHOD_NOT_ALLOWED;
            }

        } catch (NotModifiedException e) {
            rcode = HttpStatus.SC_NOT_MODIFIED;

        } catch (FileNotFoundException e) {
            rcode = HttpStatus.SC_NOT_FOUND;

        } catch (NoSuchFileException e) {
            rcode = HttpStatus.SC_NOT_FOUND;

        } catch (IOException e) {
            rcode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }

        if (rcode != 0) {
            exc.sendResponseHeaders(rcode, 0);
        }
    }

    /**
     * Send the file.
     */
    private void sendFile(Path file, HttpExchange exc) throws IOException {
        exc.sendResponseHeaders(HttpStatus.SC_OK, 0);

        // setup IO streams
        //
        try (OutputStream os = exc.getResponseBody()) {
            Files.copy(file, os);
        }
    }

    /**
     * Return the content-type the would be returned for this file name.
     */
    public String guessContentTypeFromName(String fileName) {
        return mimeTypes.getContentType(fileName);
    }

    /**
     * An exception when the source object has not been modified. While this
     * condition is not a failure, it is a break from the normal flow of
     * execution.
     */
    private static class NotModifiedException extends IOException {
        public NotModifiedException() {
        }
    }

    public void setContextPath(String path) {
        this.contextPath = path;
    }

    public String getContextPath() {
        return this.contextPath;
    }
}
