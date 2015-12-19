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

package suonos.controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.github.am0e.commons.json.ObjectWriter;
import com.github.am0e.commons.msgs.Msgs;
import com.github.am0e.webc.action.ActionCtx;
import com.github.am0e.webc.action.annotations.Action;

import suonos.controllers.resp.ZipFileResp;
import suonos.models.media.MediaTags;
import suonos.models.media.MetaDataFolder;
import suonos.models.music.MusicAlbum;
import suonos.models.music.MusicTrack;

/**
 * Albums controller.
 * 
 * GET /ws/albums/albumId GET /ws/albums GET /ws/albums/10222/tracks GET
 * /ws/albums/10222?rel=1 Get album and related tracks.
 * 
 * @author anthony
 *
 */
public final class AlbumsController extends StoredItemController<MusicAlbum> {

    public AlbumsController(ActionCtx ctx) {
        super(ctx, MusicAlbum.class);
    }

    /**
     * GET /ws/albums/10232/tracks
     * 
     * @return
     * @throws IOException
     */
    @Action
    public Object tracks() throws IOException {
        // Query the album so that a not found exception is thrown if the
        // id is invalid.
        //
        MusicAlbum album = querySvcs().getObject();
        List<MusicTrack> tracks = album.getTracks();
        return jsonData(tracks);
    }

    /**
     * GET /ws/albums/-abf2323/download
     * 
     * @return
     * @throws IOException
     */
    @Action
    public Object download() throws IOException {
        // Query the album so that a not found exception is thrown if the
        // id is invalid.
        //
        MusicAlbum album = querySvcs().getObject();
        List<MusicTrack> tracks = album.getTracks();

        ZipFileResp resp = new ZipFileResp(album.getTitle());

        for (MusicTrack track : tracks) {
            Path path = lib.getMusicLib().getTrackFolderPath(track);
            resp.addFile(path);
        }

        return resp;
    }

    /**
     * GET /ws/albums/10232/coverart?size=lg
     * 
     * @return
     * @throws IOException
     */
    @Action
    public Object coverart() throws IOException {
        String size = ctx.params().getString("size", "xs");

        // Query the album so that a not found exception is thrown if the
        // id is invalid.
        //
        MusicAlbum album = querySvcs().getObject();

        MetaDataFolder metaDataFolder = lib.getMusicLib().getMetaDataFolder(album);
        Path imageFilePath = metaDataFolder.getFilePath(Msgs.format("cover-{}.jpg", size));

        return ctx.sendFile(imageFilePath).type("image/jpeg").fileName("rhubarb.jpg");
    }

    // Thursday 26th 9:15am
    //
    // Use /ws/albums?q=artists_a:"B"&type=albumTile - summary for grids.
    // Use /ws/albums?q=artists_a:"B"&type=albumDetailed - include tracks.
    // /ws/albums?q=artists_a:"B"&target=list
    //
    protected ObjectWriter<MusicAlbum> getFormatWriter(MusicAlbum album, String format) {
        if (format.equals("object")) {
            return (jw, obj) -> {

                jw.genObjectFields(obj);

                // Add the tracks.
                //
                jw.genValue("tracks", album.getTracks());
            };
        }

        // album tile for displaying album information in a browse grid.
        //
        if (format.equals("albumTile")) {
            return (jw, obj) -> {
                jw.genValue("title", obj.getTitle());
                jw.genValue("year", obj.getTags().getTagValue(MediaTags.YEAR));
                jw.genValue("artists", obj.getTags().getTagValue(MediaTags.ARTISTS));
                jw.genValue("genres", obj.getTags().getTagValue(MediaTags.GENRES));
                jw.genValue("composers", obj.getTags().getTagValue(MediaTags.COMPOSERS));
                jw.genObjectFields(obj);

                // Add the tracks.
                //
                jw.genValue("tracks", album.getTracks());
            };
        }
        return null;

    }
}
