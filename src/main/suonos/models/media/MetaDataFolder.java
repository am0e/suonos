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
package suonos.models.media;

import java.nio.file.Path;

public class MetaDataFolder {
    /**
     * Path to the ".suonos" folder within the album/movies/photos folder.
     */
    private Path path;

    public MetaDataFolder(Path baseFolder) {
        path = baseFolder.resolve(FOLDER_NAME);
    }

    public final static String FOLDER_NAME = ".suonos";
    public final static String META_DATA_FILE_NAME = "metadata";

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Get path to a sub file.
     * 
     * @param fileName
     * @return
     */
    public Path getFilePath(String fileName) {
        return path.resolve(fileName);
    }

    public Path getMetaData() {
        return getFilePath(META_DATA_FILE_NAME);
    }
}
