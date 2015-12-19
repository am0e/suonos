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

package suonos.controllers.resp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.am0e.commons.AntLib;
import com.github.am0e.webc.WebRequest;
import com.github.am0e.webc.action.response.SendFileResponse;

import suonos.httpserver.VertxWebRequest;

public class ZipFileResp extends SendFileResponse {

    public List<Path> files;

    public ZipFileResp(String fileName) {
        super(null);
        files = new ArrayList<>();
        disposition("attachment");
        fileName(fileName + ".zip");
        type("application/zip");
    }

    public void addFile(Path fileName) {
        files.add(fileName);
    }

    private void buildZipFile(VertxWebRequest request) throws Exception {
        Map<String, String> env = AntLib.newHashMap();
        env.put("create", "true");

        // Create a new zip file.
        //
        Path tempFile = Files.createTempFile("", ".zip");

        // Kludge for newFileSystem(), it does not expect the file to exist!
        //
        Files.delete(tempFile);

        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + tempFile), env)) {

            for (Path file : files) {
                // zip path is the "./filename.ogg"
                //
                Path zipPath = fs.getPath(file.getFileName().toString());

                // Copy into the zip file.
                //
                Files.copy(file, zipPath);
            }
        }

        prepareHeaders(request);

        request.httpServerResponse().sendFile(tempFile.toString(), res -> {
            try {
                Files.delete(tempFile);

            } catch (IOException e) {
            }
        });
    }

    @Override
    public void handle(WebRequest ctx) throws Exception {

        VertxWebRequest req = (VertxWebRequest) ctx;

        req.asyncExec(request -> buildZipFile(req), result -> {
        });
    }
}
