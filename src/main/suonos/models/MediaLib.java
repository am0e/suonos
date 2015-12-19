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
package suonos.models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import suonos.app.utils.FilesUtils;
import suonos.models.media.MetaDataFolder;
import suonos.models.media.PlayableStoreObject;
import suonos.models.music.AlbumMetaData;
import suonos.models.music.MusicAlbum;
import suonos.services.MimeTypes;

public abstract class MediaLib {

    private final String code;

    @Inject
    private MimeTypes mimeTypes;

    public MediaLib(String code) {
        this.code = code;
    }

    public abstract Path getRoot();

    public abstract Path setRoot(Path path);

    public Path getRelativePath(Path path) {
        return getRoot().relativize(path);
    }

    public Path resolvePath(Path path) {
        return getRoot().resolve(path);
    }

    public AlbumMetaData getMetaData(Path relativePath) throws IOException {
        MetaDataFolder folder = new MetaDataFolder(resolvePath(relativePath));
        Path path = folder.getMetaData();

        AlbumMetaData meta = null;

        if (Files.isReadable(path)) {
            meta = FilesUtils.readObject(AlbumMetaData.class, path);
            meta.setPath(path);
        }

        return meta;
    }

    public MetaDataFolder getMetaDataFolder(MusicAlbum album) {
        return new MetaDataFolder(getAlbumFolderPath(album));
    }

    public Path getAlbumFolderPath(MusicAlbum album) {
        Path path = resolvePath(Paths.get(album.getPath()));
        return path;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    public String getMimeType(PlayableStoreObject obj) {
        return mimeTypes.getContentType(obj.getPath());
    }

}
