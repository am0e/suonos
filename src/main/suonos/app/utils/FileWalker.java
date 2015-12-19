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

package suonos.app.utils;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

import com.github.am0e.commons.AntLib;

public abstract class FileWalker {
    private Path curFolder;
    private List<Path> files = AntLib.newList();

    public void walk(Path path) throws IOException {
        // Walk the files, depth first.
        //
        Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1000, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                if (attrs.isRegularFile() && isKnownFile(file)) {

                    // Get the parent path.
                    //
                    Path folder = file.getParent();

                    // Are we still in the same folder?
                    //
                    if (folder.equals(curFolder)) {
                        // Add the track to the tracks list.
                        //
                        files.add(file);

                    } else {
                        // New folder.
                        // Clear down the tracks.
                        //
                        if (files.isEmpty() == false) {
                            processFiles(curFolder, files);
                            files.clear();
                        }

                        curFolder = folder;
                        files.add(file);
                    }
                }

                return super.visitFile(file, attrs);
            }
        });

        // Process last folder
        //
        if (files.isEmpty() == false) {
            processFiles(curFolder, files);
            files.clear();
        }
    }

    abstract protected void processFiles(Path folder, List<Path> files);

    protected boolean isKnownFile(Path file) {
        return true;
    }
}
